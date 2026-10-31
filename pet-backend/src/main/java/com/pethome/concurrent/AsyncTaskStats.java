package com.pethome.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * 异步任务指标采集器。
 *
 * 使用 LongAdder（内部按 CPU 核数分段 + CAS 更新）而不是 AtomicLong：
 * 高并发提交场景下 LongAdder 的写竞争远小于单点 CAS，读取允许最终一致，
 * 适合只用于监控上报的计数场景。
 */
public class AsyncTaskStats {

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskStats.class);

    private final String poolName;
    private final LongAdder submitted = new LongAdder();
    private final LongAdder completed = new LongAdder();
    private final LongAdder failed = new LongAdder();

    public AsyncTaskStats(String poolName) {
        this.poolName = poolName;
    }

    public void onSubmitted() {
        submitted.increment();
    }

    public void onCompleted() {
        completed.increment();
    }

    public void onFailed() {
        failed.increment();
    }

    public long submitted() {
        return submitted.sum();
    }

    public long completed() {
        return completed.sum();
    }

    public long failed() {
        return failed.sum();
    }

    /** 快照，供监控端点/日志使用 */
    public Map<String, Object> snapshot() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("pool", poolName);
        m.put("submitted", submitted());
        m.put("completed", completed());
        m.put("failed", failed());
        long pending = submitted() - completed() - failed();
        m.put("pending", Math.max(pending, 0L));
        return m;
    }

    public void logSnapshot() {
        log.info("线程池指标 {}", snapshot());
    }
}
