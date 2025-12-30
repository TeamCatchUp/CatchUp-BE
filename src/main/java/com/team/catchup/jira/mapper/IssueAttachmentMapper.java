package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.external.IssueMetadataApiResponse;
import com.team.catchup.jira.entity.IssueAttachment;
import com.team.catchup.jira.entity.IssueMetadata;
import com.team.catchup.jira.repository.IssueMetaDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class IssueAttachmentMapper {

    private static final DateTimeFormatter JIRA_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final IssueMetaDataRepository issueMetaDataRepository;

    /**
     * Issue Attachment DTO -> Entity
     */
    public IssueAttachment toEntity(IssueMetadataApiResponse.IssueAttachment attachmentResponse, Integer issueId) {
        try{
            IssueMetadata issue = issueMetaDataRepository.findById(issueId)
                    .orElseThrow(() -> new RuntimeException("Issue Not Found :" + issueId));

            return IssueAttachment.builder()
                    .id(parseInteger(attachmentResponse.id()))
                    .issueId(issue)
                    .fileName(attachmentResponse.filename())
                    .authorId(attachmentResponse.author().id() != null ?
                            attachmentResponse.author().id() : null)
                    .createdAt(parseDateTime(attachmentResponse.created()))
                    .size(parseLong(attachmentResponse.size()))
                    .mimetype(attachmentResponse.mimetype())
                    .downloadUrl(attachmentResponse.content())
                    .thumbnailUrl(attachmentResponse.thumbnail())
                    .build();
        } catch(Exception e){
            log.error("Failed to Map Attachment : {}",attachmentResponse.id());
            throw new RuntimeException("Attachment Mappling Failed", e);
        }

    }

    private Integer parseInteger(String value) {
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

    private Long parseLong(String value) {
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

    private LocalDateTime parseDateTime(String dateString) {
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
