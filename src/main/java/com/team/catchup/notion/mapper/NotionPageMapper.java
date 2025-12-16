package com.team.catchup.notion.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.catchup.notion.dto.NotionSearchResponse;
import com.team.catchup.notion.entity.NotionPage;
import com.team.catchup.notion.entity.NotionUser;
import com.team.catchup.notion.repository.NotionUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
public class NotionPageMapper {

    private static final DateTimeFormatter NOTION_DATE_FORMATTER =
            DateTimeFormatter.ISO_DATE_TIME;
    private final NotionUserRepository notionUserRepository;

    public NotionPageMapper(NotionUserRepository notionUserRepository) {
        this.notionUserRepository = notionUserRepository;
    }

    public NotionPage toEntity(NotionSearchResponse.NotionPageResult result) {
        String title = extractTitle(result.properties());

        String parentId = null;
        String parentType = null;

        if(result.parent() != null) {
            parentType = result.parent().type();
            if("page_id".equals(parentType)) {
                parentId = result.parent().pageId();
            } else if("database_id".equals(parentType)) {
                parentId = result.parent().databaseId();
            } else if("data_source_id".equals(parentType)) {
                parentId = result.parent().dataSourceId();
            } else if("workspace".equals(parentType)) {
                parentId = "workspace";
            }
        }

        NotionUser createdBy = null;
        if (result.createdBy() != null && result.createdBy().id() != null) {
            createdBy = notionUserRepository.findById(result.createdBy().id()).orElse(null);
        }

        NotionUser lastEditedBy = null;
        if (result.lastEditedBy() != null && result.lastEditedBy().id() != null) {
            lastEditedBy = notionUserRepository.findById(result.lastEditedBy().id()).orElse(null);
        }

        return NotionPage.builder()
                .pageId(result.id())
                .title(title)
                .url(result.url())
                .createdAt(parseDateTime(result.createdTime()))
                .lastEditedAt(parseDateTime(result.lastEditedTime()))
                .createdBy(createdBy)
                .lastEditedBy(lastEditedBy)
                .parentId(parentId)
                .parentType(parentType)
                .build();
    }


    private String extractTitle(Map<String, JsonNode> properties) {

        if (properties == null || properties.isEmpty()) {
            return "Untitled";
        }

        for(JsonNode property : properties.values()) {
            if (property.has("type") && property.get("type").asText().equals("title")) {
                if(property.has("title")) {
                    JsonNode titleArray = property.get("title");
                    StringBuilder fullTitle = new StringBuilder();

                    if(titleArray.isArray()) {
                        for (JsonNode textObj : titleArray) {
                            if(textObj.has("plain_text")) {
                                fullTitle.append(textObj.get("plain_text").asText());
                            }
                        }
                    }

                    return !fullTitle.isEmpty() ? fullTitle.toString() : "Untitled";
                }
            }
        }
        return "Untitled";
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        if(dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeString, NOTION_DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("DateTime 파싱 실패 : {}", dateTimeString);
            return null;
        }
    }
}

