package com.team.catchup.notion.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.notion.entity.NotionPage;
import lombok.*;

@Getter
@Builder
public class NotionRabbitRequest {
    @JsonProperty("page_id")
    private String pageId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("created_by")
    private String createdBy;

    public static NotionRabbitRequest from(NotionPage page) {
        String creatorName = (page.getCreatedBy() != null) ? page.getCreatedBy().getUserId() : "Unknown User";

        return NotionRabbitRequest.builder()
                .pageId(page.getPageId())
                .title(page.getTitle())
                .createdBy(creatorName)
                .build();
    }
}
