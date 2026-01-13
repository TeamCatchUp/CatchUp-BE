package com.team.catchup.common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "notion_sync_queue";
    public static final String GITHUB_REPOSITORY_QUEUE = "github_repository_queue";
    public static final String GITHUB_PULL_REQUEST_QUEUE = "github_pull_request_queue";
    public static final String GITHUB_ISSUE_QUEUE = "github_issue_queue";

    @Bean
    public Queue notionSyncQueue() {
        // durable: true (서버 꺼져도 큐 유지), false (꺼지면 삭제)
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    public Queue githubRepositoryQueue() {
        return new Queue(GITHUB_REPOSITORY_QUEUE, false);
    }

    @Bean
    public Queue githubPullRequestQueue() {
        return new Queue(GITHUB_PULL_REQUEST_QUEUE, false);
    }

    @Bean
    public Queue githubIssueQueue() {
        return new Queue(GITHUB_ISSUE_QUEUE, false);
    }

    @Bean
    public Jackson2JsonMessageConverter producerMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}