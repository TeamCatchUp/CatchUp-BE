package com.team.catchup.rag.service;

import com.team.catchup.rag.dto.ServerChatRequest;
import com.team.catchup.rag.dto.UserChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
public class RagService {

    private final WebClient chatClient;
    private final ChatUsageLimitService chatUsageLimitService;

    public RagService(@Qualifier("ragWebClient") WebClient chatClient, ChatUsageLimitService chatUsageLimitService) {
        this.chatClient = chatClient;
        this.chatUsageLimitService = chatUsageLimitService;
    }

    public Mono<UserChatResponse> requestChat(String query, UUID sessionId, Long memberId){
        return Mono.fromCallable(() -> chatUsageLimitService.checkAndIncrementUsageLimit(memberId, sessionId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(response -> {
                    if (response != null) {
                        return Mono.just(response);
                    }

                    ServerChatRequest request = ServerChatRequest.of(query, null, sessionId);
                    return chatClient.post()
                            .uri("/api/chat/")
                            .bodyValue(request)
                            .retrieve()
                            .onStatus(
                                    status->status.is4xxClientError() || status.is5xxServerError(),
                                    res -> res.bodyToMono(String.class)
                                            .map(RuntimeException::new)
                            )
                            .bodyToMono(UserChatResponse.class);
                });
    }
}