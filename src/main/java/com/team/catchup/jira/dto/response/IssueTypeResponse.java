package com.team.catchup.jira.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IssueTypeResponse(
        @JsonProperty("id")
        String id,

        @JsonProperty("name")
        String name,

        @JsonProperty("iconUrl")
        String iconUrl,

        @JsonProperty("subtask")
        Boolean subtask,

        @JsonProperty("hierarchyLevel")
        Integer hierarchyLevel,

        @JsonProperty("scope")
        Scope scope
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Scope(
            @JsonProperty("type")
            String type,

            @JsonProperty("project")
            Project project
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Project(
            @JsonProperty("id")
            String id
    ) {}
}