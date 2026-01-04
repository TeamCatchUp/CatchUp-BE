package com.team.catchup.rag.service;

import com.team.catchup.rag.dto.ServerChatRequest;
import com.team.catchup.rag.dto.UserChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.UUID;

@Service
public class RagService {

    private final WebClient chatClient;
    private final ChatUsageLimitService chatUsageLimitService;

    public RagService(@Qualifier("ragWebClient") WebClient chatClient, ChatUsageLimitService chatUsageLimitService) {
        this.chatClient = chatClient;
        this.chatUsageLimitService = chatUsageLimitService;
    }

    public Mono<UserChatResponse> requestChat(String query, UUID sessionId, Long memberId, String indexName) {
        return Mono.fromCallable(() -> Optional.ofNullable(chatUsageLimitService.checkAndIncrementUsageLimit(memberId, sessionId)))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalResponse -> {
                    if (optionalResponse.isPresent()) {
                        return Mono.just(optionalResponse.get());
                    }

                    ServerChatRequest request = ServerChatRequest.of(query, null, sessionId);
                    return chatClient.post()
                            .uri("/api/chat/")
                            .bodyValue(request)
                            .retrieve()
                            .onStatus(
                                    status -> status.is4xxClientError() || status.is5xxServerError(),
                                    res -> res.bodyToMono(String.class)
                                            .flatMap(error -> Mono.error(new RuntimeException(error)))
                            )
                            .bodyToMono(UserChatResponse.class);
                });
    }
}