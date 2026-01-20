package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.rag.dto.server.FastApiStreamingResponse;
import com.team.catchup.rag.dto.server.ServerJiraIssueSource;

import java.util.List;
import java.util.UUID;

/**
 * 답변 생성 과정 Streaming 마지막 응답으로 Client에게 전달할 데이터
 */

public record ClientChatStreamingFinalResponse(
        UUID sessionId,
        String type,  // result
        String node,  // generate

        @JsonProperty("response")
        ClientChatResponse clientChatResponse,  // LLM 최종 답변, 출처 포함

        List<ClientJiraIssueSource> relatedJiraIssues
) {
    public static ClientChatStreamingFinalResponse of(
            FastApiStreamingResponse dto,
            ClientChatResponse response
    ) {

        List<ServerJiraIssueSource> rawIssues = dto.getRelatedJiraIssues();

        List<ClientJiraIssueSource> relatedJiraIssues = (rawIssues == null)
                ? List.of()
                : rawIssues.stream()
                .map(ClientJiraIssueSource::from)
                .toList();

        return new ClientChatStreamingFinalResponse(
                dto.getSessionId(),
                dto.getType(),
                dto.getNode(),
                response,
                relatedJiraIssues
        );
    }
}
