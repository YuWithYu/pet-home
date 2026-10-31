package com.pethome.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 视频转码并发闸门。
 *
 * ffmpeg 是 CPU/IO 双密集型任务，单纯放大线程池只会让机器上下文切换打满。
 * 这里用 {@link Semaphore}（AQS 共享模式，state = 剩余许可数）做并发度控制：
 * 线程池负责“排队”，Semaphore 负责“限流”，两者职责分离。
 *
 * 采用 tryAcquire(timeout) 而非 acquire()：拿不到许可直接放弃转码保留原视频，
 * 避免上传接口的异步任务无限堆积。
 */
@Component
public class TranscodeGate {

    private static final Logger log = LoggerFactory.getLogger(TranscodeGate.class);

    private final int permits;
    private Semaphore semaphore;

    /** 当前占用许可数，仅用于监控展示 */
    private final AtomicInteger inFlight = new AtomicInteger();

    public TranscodeGate(@Value("${media.transcode.concurrency:2}") int permits) {
        this.permits = Math.max(1, permits);
    }

    @PostConstruct
    public void init() {
        this.semaphore = new Semaphore(permits, true);
        log.info("转码并发闸门已初始化: permits={}", permits);
    }

    /**
     * 尝试进入转码临界区
     *
     * @param timeout 最长等待时间
     * @param unit    时间单位
     * @return true 表示已获得许可，必须在 finally 中调用 {@link #exit()}
     */
    public boolean tryEnter(long timeout, TimeUnit unit) {
        try {
            boolean acquired = semaphore.tryAcquire(timeout, unit);
            if (acquired) {
                inFlight.incrementAndGet();
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** 退出转码临界区 */
    public void exit() {
        inFlight.decrementAndGet();
        semaphore.release();
    }

    public int available() {
        return semaphore.availablePermits();
    }

    public int inFlight() {
        return inFlight.get();
    }
}
