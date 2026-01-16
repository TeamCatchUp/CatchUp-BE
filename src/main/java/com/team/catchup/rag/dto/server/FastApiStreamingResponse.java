package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 가공 전의 FastAPI -> Spring 응답 결과
 */

@Getter
@NoArgsConstructor
public class FastApiStreamingResponse {
    private String type;
    private String node;
    private String message;  // type = status
    private String answer;   // type = result
    private List<PullRequestCandidate> payload;  // typee = interrupt
    private List<ServerSource> sources;
    @JsonProperty("process_time")
    private Double processTime;
}
