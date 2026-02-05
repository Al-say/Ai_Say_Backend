package com.zhupinzan.speaking.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务执行器配置
 * <p>
 * <b>设计目标：</b>为异步 AI 评估任务提供高性能、可控的线程池。
 * <p>
 * <b>核心配置：</b><br>
 * • 核心线程数：5（常驻线程）<br>
 * • 最大线程数：20（高峰期扩展）<br>
 * • 队列容量：100（缓冲待处理任务）<br>
 * • 拒绝策略：CallerRunsPolicy（回退到主线程）
 * <p>
 * <b>性能考虑：</b><br>
 * • AI 评估是 IO 密集型（等待 DeepSeek 响应）<br>
 * • 线程数可适当大于 CPU 核心数<br>
 * • 避免创建过多线程导致上下文切换开销
 * <p>
 * <b>监控指标：</b><br>
 * • 活跃线程数<br>
 * • 队列长度<br>
 * • 任务拒绝次数<br>
 * • 平均任务执行时间
 *
 * @author system
 * @since 1.0.0
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 自定义异步任务执行器
     * <p>
     * <b>线程池参数说明：</b><br>
     * • <b>corePoolSize</b>: 核心线程数，即使空闲也会保留<br>
     * • <b>maxPoolSize</b>: 最大线程数，高峰期可扩展到的最大值<br>
     * • <b>queueCapacity</b>: 任务队列容量，超过则创建新线程<br>
     * • <b>keepAliveSeconds</b>: 空闲线程存活时间（秒）<br>
     * • <b>rejectedExecutionHandler</b>: 任务拒绝策略
     * </p>
     * <p>
     * <b>拒绝策略选择：</b><br>
     * • CallerRunsPolicy：在调用者线程执行（降级方案）<br>
     * • AbortPolicy：抛出异常（默认，不推荐）<br>
     * • DiscardPolicy：静默丢弃（可能丢失数据）<br>
     * • DiscardOldestPolicy：丢弃队列最旧的任务
     * </p>
     *
     * @return 配置好的线程池执行器
     */
    @Bean(name = "asyncTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数（CPU 密集型建议 N+1，IO 密集型建议 2N）
        executor.setCorePoolSize(5);

        // 最大线程数（根据服务器资源和业务需求调整）
        executor.setMaxPoolSize(20);

        // 队列容量（平衡内存占用和任务缓冲能力）
        executor.setQueueCapacity(100);

        // 线程名前缀（方便日志追踪和问题定位）
        executor.setThreadNamePrefix("async-eval-");

        // 空闲线程存活时间（60 秒后回收多余线程）
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：队列满时在调用者线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池（优雅停机）
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 最长等待时间（避免无限等待）
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("异步任务执行器初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
