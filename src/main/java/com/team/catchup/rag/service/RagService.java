package com.team.catchup.rag.service;

import com.team.catchup.rag.dto.ServerChatRequest;
import com.team.catchup.rag.dto.UserChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class RagService {

    private final WebClient chatClient;

    public RagService(@Qualifier("ragWebClient") WebClient chatClient) {
        this.chatClient = chatClient;
    }

    public Mono<UserChatResponse> requestChat(String query, UUID sessionId){
        ServerChatRequest request = ServerChatRequest.of(query, null, sessionId);
        return chatClient.post()
                .uri("/api/chat/")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status->status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(RuntimeException::new)
                )
                .bodyToMono(UserChatResponse.class);
    }
}
