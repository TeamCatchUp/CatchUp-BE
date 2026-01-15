package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 답변 생성 과정 Streaming 마지막 응답으로 Client에게 전달할 데이터
 */

public record ClientChatStreamingFinalResponse(
        String type,  // result
        String node,  // generate

        @JsonProperty("response")
        ClientChatResponse clientChatResponse  // LLM 최종 답변, 출처 포함
) {
    public static ClientChatStreamingFinalResponse of(
            String type,
            String node,
            ClientChatResponse response
    ) {
        return new ClientChatStreamingFinalResponse(type, node, response);
    }
}
