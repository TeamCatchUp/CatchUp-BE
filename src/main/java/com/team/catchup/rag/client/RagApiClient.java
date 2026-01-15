package com.team.catchup.rag.client;

import com.team.catchup.rag.dto.server.FastApiStreamingResponse;
import com.team.catchup.rag.dto.server.ServerChatRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RagApiClient {
    private final WebClient chatClient;

    public RagApiClient(@Qualifier("ragWebClient") WebClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 채팅 요청. 답변 생성 과정 (예: '검색 중...', '답변 생성 중...') 스트리밍을 지원한다.
     */
    public Flux<FastApiStreamingResponse> requestChatStream(ServerChatRequest request) {
        return chatClient.post()
                .uri("/api/chat/stream")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        res -> res.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("Rag Server 오류: " + error)))
                )
                .bodyToFlux(FastApiStreamingResponse.class);
    }
}
