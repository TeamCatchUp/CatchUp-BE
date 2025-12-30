package com.team.catchup.notion.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.catchup.notion.dto.external.NotionSearchApiResponse;
import com.team.catchup.notion.entity.NotionPage;
import com.team.catchup.notion.entity.NotionUser;
import com.team.catchup.notion.repository.NotionUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotionPageMapper {

    private static final DateTimeFormatter NOTION_DATE_FORMATTER =
            DateTimeFormatter.ISO_DATE_TIME;
    private final NotionUserRepository notionUserRepository;

    public NotionPage toEntity(NotionSearchApiResponse.NotionPageResult result) {
        try {
            String title = extractTitle(result.properties());
            String parentId = extractParentId(result.parent());
            String parentType = result.parent() != null
                    ? result.parent().type()
                    : null;

            NotionUser createdBy = findUser(result.createdBy());
            NotionUser lastEditedBy = findUser(result.lastEditedBy());

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
        }catch (Exception e) {
            log.error("Failed to map NotionPage: {}", result.id(), e);
            throw new RuntimeException("Failed to map NotionPage: " + result.id(), e);
        }
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

    private String extractParentId(NotionSearchApiResponse.Parent parent) {
        if (parent == null){
            return null;
        }

        String parentType = parent.type();
        return switch (parentType) {
            case "page_id" -> parent.pageId();
            case "database_id" -> parent.databaseId();
            case "data_source_id" -> parent.dataSourceId();
            case "workspace" -> "workspace";
            default -> null;
        };
    }

    private NotionUser findUser(NotionSearchApiResponse.User user) {
        if(user != null && user.id() != null) {
            return notionUserRepository.findById(user.id()).orElse(null);
        }
        return null;
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

