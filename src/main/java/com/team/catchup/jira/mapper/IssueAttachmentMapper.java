package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import com.team.catchup.jira.entity.IssueAttachment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class IssueAttachmentMapper {

    private static final DateTimeFormatter JIRA_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * Issue Attachment DTO -> Entity
     */
    public IssueAttachment IssueAttachmentToEntity(IssueMetaDataResponse.IssueAttachment attachmentResponse, Integer issueId) {
        return IssueAttachment.builder()
                .id(parseIntegerSafely(attachmentResponse.id()))
                .issueId(issueId)
                .fileName(attachmentResponse.filename())
                .authorId(attachmentResponse.author().id())
                .createdAt(parseDateTimeSafely(attachmentResponse.created()))
                .size(parseLongSafely(attachmentResponse.size()))
                .mimetype(attachmentResponse.mimetype())
                .downloadUrl(attachmentResponse.content())
                .thumbnailUrl(attachmentResponse.thumbnail())
                .build();
    }

    private Integer parseIntegerSafely(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Failed Parsing String to Integer: {}", value);
            return null;
        }
    }

    private Long parseLongSafely(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Failed Parsing String to Long: {}", value);
            return null;
        }
    }

    private LocalDateTime parseDateTimeSafely(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, JIRA_DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Failed Parsing String to Local Date Time: {}", dateString);
            return null;
        }
    }
}
