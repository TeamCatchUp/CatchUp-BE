package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.dto.server.FastApiStreamingResponse;

/**
 * 최종 답변 생성 과정 외의 답변 생성 과정 Streaming 시에 Client에 전달할 데이터
 */

public record ClientChatStreamingResponse(
        String type,
        String node,
        String message
) {
    /**
     * DB에 저장되지 않는 정보이므로, FastAPI 응답을 바로 활용
     */
    public static ClientChatStreamingResponse from(FastApiStreamingResponse dto) {
        return new ClientChatStreamingResponse(
                dto.getType(),
                dto.getNode(),
                dto.getMessage()
        );
    }
}
