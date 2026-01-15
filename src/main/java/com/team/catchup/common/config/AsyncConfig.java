package com.team.catchup.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "meiliSearchExecutor")
    public Executor meiliSearchExecutor() {
        ThreadPoolTaskExecutor  executor = new ThreadPoolTaskExecutor();

        // 기본 스레드 5개
        executor.setCorePoolSize(5);

        // 기본 스레드 개수를 초과하면 10개까지 확장
        executor.setMaxPoolSize(10);

        // 10개를 초과하면 최대 50개까지 Queue에서 대기
        executor.setQueueCapacity(50);

        // 로그 상 식별을 위한 Prefix
        executor.setThreadNamePrefix("MeiliSearch-Async-");

        executor.initialize();
        return executor;
    }

    @Bean(name = "ragExecutor")
    public Executor regExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 기본 스레드 5개
        executor.setCorePoolSize(5);

        // 기본 스레드 개수를 초과하면 10개까지 확장
        executor.setMaxPoolSize(10);

        // 10개를 초과하면 최대 50개까지 Queue에서 대기
        executor.setQueueCapacity(50);

        // 로그 상 식별을 위한 Prefix
        executor.setThreadNamePrefix("RagAsync-");

        executor.initialize();
        return executor;
    }
}
