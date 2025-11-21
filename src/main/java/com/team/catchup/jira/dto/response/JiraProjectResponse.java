package com.team.catchup.jira.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraProjectResponse(
        @JsonProperty("self")
        String self,

        @JsonProperty("maxResults")
        Integer maxResults,

        @JsonProperty("startAt")
        Integer startAt,

        @JsonProperty("total")
        Integer total,

        @JsonProperty("isLast")
        Boolean isLast,

        @JsonProperty("values")
        List<ProjectValue> values
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProjectValue(
            @JsonProperty("id")
            String id,

            @JsonProperty("key")
            String key,

            @JsonProperty("name")
            String name,

            @JsonProperty("description")
            String description,

            @JsonProperty("projectTypeKey")
            String projectTypeKey,

            @JsonProperty("style")
            String style,

            @JsonProperty("simplified")
            Boolean simplified,

            @JsonProperty("isPrivate")
            Boolean isPrivate,

            @JsonProperty("avatarUrls")
            AvatarUrls avatarUrls,

            @JsonProperty("self")
            String self,

            @JsonProperty("insight")
            Insight insight
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AvatarUrls(
            @JsonProperty("48x48")
            String avatar48
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Insight(
            @JsonProperty("totalIssueCount")
            Integer totalIssueCount,

            @JsonProperty("lastIssueUpdateTime")
            String lastIssueUpdateTime
    ) {}
}