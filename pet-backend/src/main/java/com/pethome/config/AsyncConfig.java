package com.pethome.config;

import com.pethome.concurrent.AsyncTaskStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置。
 *
 * 设计要点：
 * 1. 业务池与媒体池物理隔离：视频转码是长耗时 CPU/IO 任务，与短信、通知等短任务共池会拖垮响应；
 * 2. 有界队列 + CallerRunsPolicy：队列打满后由提交线程执行，形成天然背压，避免 OOM 与任务静默丢弃；
 * 3. 拒绝策略、线程名前缀、优雅停机全部显式声明，便于线上排查（线程名可直接定位来源）；
 * 4. 通过 TaskDecorator 挂载 LongAdder 指标，统计提交/完成/失败，无需侵入业务代码。
 */
@Configuration
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    public static final String MEDIA_EXECUTOR = "mediaExecutor";
    public static final String BIZ_EXECUTOR = "bizExecutor";

    @Bean(name = MEDIA_EXECUTOR)
    public ThreadPoolTaskExecutor mediaExecutor() {
        return buildExecutor("media-", 2, 4, 50, mediaStats());
    }

    @Bean(name = BIZ_EXECUTOR)
    public ThreadPoolTaskExecutor bizExecutor() {
        return buildExecutor("biz-", 4, 8, 500, bizStats());
    }

    @Bean
    public AsyncTaskStats mediaStats() {
        return new AsyncTaskStats(MEDIA_EXECUTOR);
    }

    @Bean
    public AsyncTaskStats bizStats() {
        return new AsyncTaskStats(BIZ_EXECUTOR);
    }

    private ThreadPoolTaskExecutor buildExecutor(String threadPrefix, int core, int max, int queueCapacity,
                                                 AsyncTaskStats stats) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix(threadPrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setTaskDecorator(runnable -> {
            stats.onSubmitted();
            return () -> {
                try {
                    runnable.run();
                    stats.onCompleted();
                } catch (Throwable t) {
                    stats.onFailed();
                    throw t;
                }
            };
        });
        log.info("线程池创建: prefix={}, core={}, max={}, queue={}", threadPrefix, core, max, queueCapacity);
        return executor;
    }

    /** 每 10 分钟输出一次线程池指标，便于容量评估与告警接入 */
    @Scheduled(cron = "0 */10 * * * ?")
    public void reportPoolStats() {
        mediaStats().logSnapshot();
        bizStats().logSnapshot();
    }
}
