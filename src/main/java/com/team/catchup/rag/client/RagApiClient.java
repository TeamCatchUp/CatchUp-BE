package com.team.catchup.rag.client;

import com.team.catchup.rag.dto.server.ServerChatRequest;
import com.team.catchup.rag.dto.server.ServerChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class RagApiClient {
    private final WebClient chatClient;

    public RagApiClient(@Qualifier("ragWebClient") WebClient chatClient) {
        this.chatClient = chatClient;
    }

    public Mono<ServerChatResponse> requestChat(ServerChatRequest request){
        return chatClient.post()
                .uri("/api/chat")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        res -> res.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("Rag Server 오류: " + error)))
                )
                .bodyToMono(ServerChatResponse.class);
    }
}
