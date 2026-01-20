package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 가공 전의 FastAPI -> Spring 응답 결과
 */

@Getter
@NoArgsConstructor
public class FastApiStreamingResponse {
    @JsonProperty("session_id")
    private UUID sessionId;

    private String type;

    private String node;

    private String message;  // type = status

    private String answer;   // type = result

    private List<PullRequestCandidate> payload;  // typee = interrupt

    private List<ServerSource> sources;

    @JsonProperty("related_jira_issues")
    private List<ServerJiraIssueSource> relatedJiraIssues;  // type = result

    @JsonProperty("process_time")
    private Double processTime;
}
