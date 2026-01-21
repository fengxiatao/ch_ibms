package cn.iocoder.yudao.module.iot.newgateway.core.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 网关线程池配置
 * <p>
 * 提供设备命令执行的专用线程池，用于隔离 SDK 类设备的同步阻塞调用，
 * 避免阻塞 RocketMQ 消费线程。
 * </p>
 *
 * <p>设计原则：</p>
 * <ul>
 *     <li>SDK 命令（门禁、NVR）使用专用线程池执行</li>
 *     <li>TCP/Netty 命令（报警、长辉）直接在消费线程执行（非阻塞）</li>
 *     <li>队列满时使用 CallerRunsPolicy，确保命令不丢失</li>
 * </ul>
 *
 * @author IoT Gateway Team
 */
@Configuration
@Slf4j
public class GatewayExecutorConfig {

    /**
     * SDK 命令执行线程池
     * <p>
     * 用于执行门禁、NVR 等 SDK 类设备的命令，这些命令可能同步阻塞 5-10 秒。
     * </p>
     *
     * <p>配置说明：</p>
     * <ul>
     *     <li>corePoolSize=8：常驻线程数，支持 8 个并发 SDK 操作</li>
     *     <li>maxPoolSize=16：最大线程数，峰值时可扩展到 16 个</li>
     *     <li>queueCapacity=100：等待队列，缓冲突发流量</li>
     *     <li>keepAliveSeconds=60：空闲线程存活时间</li>
     *     <li>rejectedExecutionHandler=CallerRunsPolicy：队列满时在调用者线程执行，避免丢失命令</li>
     * </ul>
     */
    @Bean("sdkCommandExecutor")
    public Executor sdkCommandExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：常驻 8 个线程处理 SDK 命令
        executor.setCorePoolSize(8);
        
        // 最大线程数：峰值可扩展到 16 个
        executor.setMaxPoolSize(16);
        
        // 队列容量：缓冲 100 个待执行命令
        executor.setQueueCapacity(100);
        
        // 空闲线程存活时间：60 秒
        executor.setKeepAliveSeconds(60);
        
        // 线程名前缀：便于监控和排查
        executor.setThreadNamePrefix("sdk-cmd-");
        
        // 拒绝策略：队列满时在调用者线程执行，确保命令不丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 允许核心线程超时销毁
        executor.setAllowCoreThreadTimeOut(true);
        
        // 等待任务完成后再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        log.info("[GatewayExecutorConfig] SDK 命令执行线程池已创建: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), 100);
        
        return executor;
    }

    /**
     * 通用设备命令执行线程池
     * <p>
     * 用于执行 TCP/Netty 类设备的命令（报警、长辉），这些命令通常非阻塞。
     * 配置较轻量，主要用于异步化命令执行流程。
     * </p>
     */
    @Bean("deviceCommandExecutor")
    public Executor deviceCommandExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：4 个
        executor.setCorePoolSize(4);
        
        // 最大线程数：8 个
        executor.setMaxPoolSize(8);
        
        // 队列容量：200 个
        executor.setQueueCapacity(200);
        
        // 空闲线程存活时间：60 秒
        executor.setKeepAliveSeconds(60);
        
        // 线程名前缀
        executor.setThreadNamePrefix("dev-cmd-");
        
        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        log.info("[GatewayExecutorConfig] 设备命令执行线程池已创建: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), 200);
        
        return executor;
    }
}
