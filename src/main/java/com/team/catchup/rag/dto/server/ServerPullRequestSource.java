package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class ServerPullRequestSource extends ServerSource{
    private String title;

    @JsonProperty("pr_number")
    private Integer prNumber;

    private String state;

    @JsonProperty("created_at")
    private Long createdAt;

    private String author;
}
