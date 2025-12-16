package com.team.catchup.notion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotionUserResponse(
        @JsonProperty("results")
        List<NotionUserResult> results,

        @JsonProperty("next_cursor")
        String nextCursor,

        @JsonProperty("has_more")
        boolean hasMore
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NotionUserResult(
            String id,
            String type, // person / bot 중 하나 -> person만 저장 !
            String name,

            @JsonProperty("avatar_url")
            String avatarUrl,

            Person person
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Person(
            String email
    ) {}
}