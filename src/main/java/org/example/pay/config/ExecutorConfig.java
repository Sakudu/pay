package org.example.pay.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author gwd
 * @date 2023/11/10 08:51
 */
@Slf4j
@EnableAsync
@Configuration
public class ExecutorConfig {

    private static final int QUEUE_CAPACITY = 100;
    private static final int KEEP_ALIVE_TIME = 60;

    @Bean(name = "asyncServiceExecutor")
    public Executor asyncServiceExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int threadCount = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(threadCount * 2 + 1);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_TIME);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("pay-");
        executor.initialize();
        return executor;
    }
}
