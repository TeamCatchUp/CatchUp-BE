package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.dto.server.FastApiStreamingResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * 최종 답변 생성 과정 외의 답변 생성 과정 Streaming 시에 Client에 전달할 데이터
 */

@Builder
public record ClientChatStreamingResponse(
        UUID sessionId,
        String type,
        String node,
        String message,
        List<ClientPullRequestCandidate> payload
) {
    /**
     * DB에 저장되지 않는 정보이므로, FastAPI 응답을 바로 활용
     */
    public static ClientChatStreamingResponse from(FastApiStreamingResponse dto) {
        return ClientChatStreamingResponse.builder()
                .sessionId(dto.getSessionId())
                .type(dto.getType())
                .node(dto.getNode())
                .message(dto.getMessage())
                .build();
    }

    public static ClientChatStreamingResponse createInterruptResponse(FastApiStreamingResponse dto) {
        List<ClientPullRequestCandidate> candidates = dto.getPayload().stream()
                .map(ClientPullRequestCandidate::from)
                .toList();

        return ClientChatStreamingResponse.builder()
                .sessionId(dto.getSessionId())
                .type(dto.getType())
                .node(dto.getNode())
                .payload(candidates)
                .build();
    }
}
