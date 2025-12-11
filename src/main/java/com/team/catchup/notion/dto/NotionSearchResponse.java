package com.team.catchup.notion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotionSearchResponse(
        @JsonProperty("results") List<NotionPageResult> results,
        @JsonProperty("next_cursor") String nextCursor,
        @JsonProperty("has_more") boolean hasMore
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NotionPageResult(
            String id,
            String url,

            @JsonProperty("created_time")
            String createdTime,

            @JsonProperty("last_edited_time")
            String lastEditedTime,

            @JsonProperty("created_by")
            User createdBy,

            @JsonProperty("last_edited_by")
            User lastEditedBy,

            Parent parent,

            @JsonProperty("properties")
            Map<String, JsonNode> properties
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(
            String id
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Parent(
            String type,
            @JsonProperty("page_id") String pageId,
            @JsonProperty("database_id") String databaseId,
            @JsonProperty("data_source_id") String dataSourceId,
            @JsonProperty("workspace") Boolean workspace
    ) {}
}