package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import com.team.catchup.jira.entity.IssueMetadata;
import com.team.catchup.jira.entity.IssueType;
import com.team.catchup.jira.entity.JiraProject;
import com.team.catchup.jira.entity.JiraUser;
import com.team.catchup.jira.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class IssueMetaDataMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final IssueTypeRepository issueTypeRepository;
    private final JiraProjectRepository jiraProjectRepository;
    private final JiraUserRepository jiraUserRepository;

    public IssueMetadata toEntity(IssueMetaDataResponse.JiraIssue jiraIssue) {
        try {
            IssueMetaDataResponse.Fields fields = jiraIssue.fields();

            IssueType issueType = null;
            if (fields.issueType() != null && fields.issueType().id() != null) {
                Integer issueTypeId = parseIntegerSafely(fields.issueType().id());
                issueType = issueTypeRepository.findById(issueTypeId).orElse(null);
                if (issueType == null) {
                    log.warn("IssueType not found: {}", issueTypeId);
                }
            }

            JiraProject jiraProject = null;
            if (fields.project() != null && fields.project().projectId() != null) {
                Integer projectId = parseIntegerSafely(fields.project().projectId());
                jiraProject = jiraProjectRepository.findById(projectId).orElse(null);
                if (jiraProject == null) {  // ✅ issueType -> jiraProject 수정
                    log.warn("Project not found: {}", projectId);
                }
            }

            JiraUser creator = null;
            if (fields.creator() != null && fields.creator().id() != null) {
                creator = jiraUserRepository.findById(fields.creator().id()).orElse(null);
            }

            JiraUser reporter = null;
            if (fields.reporter() != null && fields.reporter().id() != null) {
                reporter = jiraUserRepository.findById(fields.reporter().id()).orElse(null);
            }

            JiraUser assignee = null;
            if (fields.assignee() != null && fields.assignee().id() != null) {
                assignee = jiraUserRepository.findById(fields.assignee().id()).orElse(null);
            }

            return IssueMetadata.builder()
                    .issueId(parseIntegerSafely(jiraIssue.id()))
                    .issueKey(jiraIssue.key())
                    .self(jiraIssue.self())
                    .issueType(issueType)
                    .project(jiraProject)
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
                    .creator(creator)
                    .reporter(reporter)
                    .assignee(assignee)
                    .build();

        } catch (Exception e) {
            log.error("Failed to map issue: {}", jiraIssue.key(), e);
            throw new RuntimeException("Issue mapping failed: " + jiraIssue.key(), e);
        }
    }

    private Integer parseIntegerSafely(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer: {}", value);
            return null;
        }
    }

    private LocalDateTime parseDateTimeSafely(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse datetime: {}", dateTimeString);
            return null;
        }
    }
}