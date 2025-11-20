package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import com.team.catchup.jira.entity.IssueMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class IssueMetaDataMapper {

    private static final DateTimeFormatter JIRA_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public IssueMetadata toEntity(IssueMetaDataResponse.JiraIssue jiraIssue) {
        try {
            IssueMetaDataResponse.Fields fields = jiraIssue.fields();

            return IssueMetadata.builder()
                    .issueId(parseIntegerSafely(jiraIssue.id()))
                    .issueKey(jiraIssue.key())
                    .self(jiraIssue.self())
                    .issueTypeId(parseIntegerSafely(fields.issueType().id()))
                    .projectId(parseIntegerSafely(fields.project().projectId()))
                    .summary(fields.summary())
                    .description(fields.description())
                    .parentIssueId(fields.parentIssue() != null ?
                            parseIntegerSafely(fields.parentIssue().parentIssueId()) : null)
                    .statusId(parseIntegerSafely(fields.statusCategory().statusId()))
                    .priorityId(fields.issuePriority() != null ?
                            parseIntegerSafely(fields.issuePriority().priorityId()) : null)
                    .duedate(parseDateTimeSafely(fields.issueDueDate()))
                    .issueCreatedAt(parseDateTimeSafely(fields.issueCreatedAt()))
                    .resolutionId(fields.issueResolution() != null ?
                            parseIntegerSafely(fields.issueResolution().resolutionId()) : null)
                    .resolutionDate(parseDateTimeSafely(fields.resolutionDate()))
                    .creatorId(fields.creator() != null ? fields.creator().id() : null)
                    .reporterId(fields.reporter() != null ? fields.reporter().id() : null)
                    .assigneeId(fields.assignee() != null ? fields.assignee().id() : null)
                    .build();
        } catch (Exception e) {
            log.error("[JIRA] 매핑 실패: {}", jiraIssue.key(), e);
            throw new RuntimeException("[JIRA] 이슈 매핑 실패: " + jiraIssue.key(), e);
        }
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

    private LocalDateTime parseDateTimeSafely(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, JIRA_DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Failed Parsing String to LocalDateTime: {}", dateString);
            return null;
        }
    }
}