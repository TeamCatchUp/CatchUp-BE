package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.dto.server.FastApiStreamingResponse;
import com.team.catchup.rag.dto.server.PullRequestCandidate;
import lombok.Builder;

import java.util.List;

/**
 * 최종 답변 생성 과정 외의 답변 생성 과정 Streaming 시에 Client에 전달할 데이터
 */

@Builder
public record ClientChatStreamingResponse(
        String type,
        String node,
        String message,
        List<PullRequestCandidate> payload
) {
    /**
     * DB에 저장되지 않는 정보이므로, FastAPI 응답을 바로 활용
     */
    public static ClientChatStreamingResponse from(FastApiStreamingResponse dto) {
        return ClientChatStreamingResponse.builder()
                .type(dto.getType())
                .node(dto.getNode())
                .message(dto.getMessage())
                .build();
    }

    public static ClientChatStreamingResponse createInterruptResponse(FastApiStreamingResponse dto) {
        return ClientChatStreamingResponse.builder()
                .type(dto.getType())
                .node(dto.getNode())
                .payload(dto.getPayload())
                .build();
    }
}
