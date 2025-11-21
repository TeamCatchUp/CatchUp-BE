package com.team.catchup.jira.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(JiraProperties.class)
public class WebClientConfig {

    private final JiraProperties jiraProperties;

    @Bean
    public WebClient jiraWebClient() {
        log.info("=== Jira Properties 확인 ===");
        log.info("Base URL: {}", jiraProperties.getBaseUrl());
        log.info("Email: {}", jiraProperties.getEmail());
        log.info("API Token: {}", jiraProperties.getApiToken() != null ? "EXISTS (length: " + jiraProperties.getApiToken().length() + ")" : "NULL");

        // Email + API Token -> Base 64 Encoding
        String credentials = jiraProperties.getEmail() + ":" + jiraProperties.getApiToken();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        // HTTP Connection Pool 설정
        // 현재 설정 100 커넥션, 타임아웃 45초 -> 테스트해보고 재설정
        // 타임아웃 : 커넥션 풀에 있는 커넥션을 잡는것을 대기하는 시간
        ConnectionProvider connectionProvider = ConnectionProvider.builder("jira-connection-pool")
                .maxConnections(jiraProperties.getConnection().getMaxConnections())
                .pendingAcquireTimeout(Duration.ofMillis(jiraProperties.getConnection().getPendingAcquireTimeout()))
                .build();

        // 커넥션 타임아웃 : TCP 연결을 잡는데 대기하는 시간
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, jiraProperties.getTimeout().getConnect())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(jiraProperties.getTimeout().getRead(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(jiraProperties.getTimeout().getWrite(), TimeUnit.MILLISECONDS))
                );

        return WebClient.builder()
                .baseUrl(jiraProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)  // ← 수정
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(jiraProperties.getMemory().getMaxInMemorySize()))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }


    // WebClient 요청/응답 사이에 끼울 수 있는 인터셉터
    // 이후에 모니터링도 추가하면 좋을 것 같아용
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("[Request] {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("[Response] {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
