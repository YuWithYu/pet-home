package com.pethome.common.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * 基于 Redis 的分布式锁（Redisson 同类实现的最小化版本）。
 *
 * 关键点：
 * 1. 加锁：SET key owner NX PX ttl，一步原子完成“判重 + 过期”，避免旧实现
 *    setIfAbsent 之后再 expire 的两步竞态（进程在两步之间宕机 → 死锁）；
 * 2. 解锁：Lua 脚本比对 owner 后删除，保证“判断归属”与“删除”原子执行，
 *    杜绝 A 请求误删 B 请求锁的经典问题；
 * 3. 续期：内置看门狗（独立守护线程），按 ttl/3 周期续期，业务执行时间超过
 *    ttl 也不会被提前释放；续期失败即判定锁丢失，后续释放不再删除他人锁；
 * 4. 自旋获取：waitTime 内按固定间隔重试，避免请求直接失败造成用户体验抖动。
 */
@Service
public class RedisDistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    /** 释放：仅删除属于自己的锁 */
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    /** 续期：仅当锁仍属于自己时重置过期时间 */
    private static final DefaultRedisScript<Long> RENEW_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end",
            Long.class);

    private static final long RETRY_INTERVAL_MILLIS = 100L;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /** 看门狗线程池：单线程足够，任务本身只做一次 pexpire */
    private final ScheduledThreadPoolExecutor watchdog = new ScheduledThreadPoolExecutor(2, r -> {
        Thread t = new Thread(r, "redis-lock-watchdog");
        t.setDaemon(true);
        return t;
    });

    /**
     * 尝试获取锁
     *
     * @param key      锁 key
     * @param ttl      锁初始过期时间（必须设置，避免宕机死锁）
     * @param waitTime 最长等待时间，0 表示只尝试一次
     * @return 锁句柄；获取失败返回 null
     */
    public LockHandle tryLock(String key, Duration ttl, Duration waitTime) {
        String owner = UUID.randomUUID().toString();
        long deadline = System.currentTimeMillis() + Math.max(waitTime.toMillis(), 0);
        boolean acquired;
        do {
            acquired = tryAcquire(key, owner, ttl);
            if (acquired) {
                break;
            }
            if (System.currentTimeMillis() >= deadline) {
                return null;
            }
            try {
                Thread.sleep(RETRY_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        } while (System.currentTimeMillis() < deadline);

        RedisLockHandle handle = new RedisLockHandle(key, owner, ttl);
        handle.startWatchdog();
        return handle;
    }

    /**
     * 加锁执行模板：成功执行后自动释放，异常时同样释放并抛出。
     *
     * @throws IllegalStateException 获取锁失败
     */
    public <T> T executeWithLock(String key, Duration ttl, Duration waitTime, Supplier<T> action) {
        LockHandle handle = tryLock(key, ttl, waitTime);
        if (handle == null) {
            throw new IllegalStateException("获取锁失败，请稍后重试: " + key);
        }
        try (handle) {
            return action.get();
        }
    }

    public void executeWithLock(String key, Duration ttl, Duration waitTime, Runnable action) {
        executeWithLock(key, ttl, waitTime, () -> {
            action.run();
            return null;
        });
    }

    /**
     * 抢占式执行：仅当本次成功抢到锁才执行（不等待）。
     * 用于多实例部署下定时任务的“单实例执行”语义，避免同一批次任务被重复跑。
     *
     * @return true 表示本次抢锁成功并已执行
     */
    public boolean tryRun(String key, Duration ttl, Runnable action) {
        LockHandle handle = tryLock(key, ttl, Duration.ZERO);
        if (handle == null) {
            return false;
        }
        try (handle) {
            action.run();
            return true;
        }
    }

    private boolean tryAcquire(String key, String owner, Duration ttl) {
        try {
            Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, owner, ttl);
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            log.warn("获取分布式锁异常: key={}, err={}", key, e.getMessage());
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        watchdog.shutdownNow();
    }

    /** 单个锁实例：持有 owner 与看门狗任务 */
    private class RedisLockHandle implements LockHandle {

        private final String key;
        private final String owner;
        private final Duration ttl;
        private final AtomicBoolean lost = new AtomicBoolean(false);
        private volatile java.util.concurrent.ScheduledFuture<?> renewTask;

        RedisLockHandle(String key, String owner, Duration ttl) {
            this.key = key;
            this.owner = owner;
            this.ttl = ttl;
        }

        void startWatchdog() {
            long period = Math.max(ttl.toMillis() / 3, 1000L);
            renewTask = watchdog.scheduleAtFixedRate(this::safeRenew, period, period, TimeUnit.MILLISECONDS);
        }

        private void safeRenew() {
            if (!renew()) {
                lost.set(true);
                cancelRenew();
                log.warn("分布式锁续期失败，判定锁已丢失: key={}, owner={}", key, owner);
            }
        }

        private void cancelRenew() {
            if (renewTask != null) {
                renewTask.cancel(false);
            }
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public String owner() {
            return owner;
        }

        @Override
        public boolean isValid() {
            return !lost.get();
        }

        @Override
        public boolean renew() {
            try {
                Long r = stringRedisTemplate.execute(RENEW_SCRIPT,
                        Collections.singletonList(key), owner, String.valueOf(ttl.toMillis()));
                return r != null && r > 0;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void close() {
            cancelRenew();
            if (lost.get()) {
                return;
            }
            try {
                stringRedisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(key), owner);
            } catch (Exception e) {
                log.warn("释放分布式锁异常: key={}, err={}", key, e.getMessage());
            }
        }
    }
}
