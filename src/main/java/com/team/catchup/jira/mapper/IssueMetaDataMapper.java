package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.external.IssueMetadataApiResponse;
import com.team.catchup.jira.entity.IssueMetadata;
import com.team.catchup.jira.entity.IssueType;
import com.team.catchup.jira.entity.JiraProject;
import com.team.catchup.jira.entity.JiraUser;
import com.team.catchup.jira.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class IssueMetaDataMapper {

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final IssueTypeRepository issueTypeRepository;
    private final JiraProjectRepository jiraProjectRepository;
    private final JiraUserRepository jiraUserRepository;

    public IssueMetadata toEntity(IssueMetadataApiResponse.JiraIssue jiraIssue) {
        try {
            IssueMetadataApiResponse.Fields fields = jiraIssue.fields();

            IssueType issueType = findIssueType(fields);
            JiraProject jiraProject = findJiraProject(fields);
            JiraUser creator = findUser(fields.creator());
            JiraUser reporter = findUser(fields.reporter());
            JiraUser assignee = findUser(fields.assignee());

            return IssueMetadata.builder()
                    .issueId(parseInteger(jiraIssue.id()))
                    .issueKey(jiraIssue.key())
                    .self(jiraIssue.self())
                    .issueType(issueType)
                    .project(jiraProject)
                    .summary(fields.summary())
                    .parentIssueId(fields.parentIssue() != null ?
                            parseInteger(fields.parentIssue().parentIssueId()) : null)
                    .statusId(parseInteger(fields.statusCategory().statusId()))
                    .priorityId(fields.issuePriority() != null ?
                            parseInteger(fields.issuePriority().priorityId()) : null)
                    .duedate(parseDateTime(fields.issueDueDate()))
                    .issueCreatedAt(parseDateTime(fields.issueCreatedAt()))
                    .resolutionId(fields.issueResolution() != null ?
                            parseInteger(fields.issueResolution().resolutionId()) : null)
                    .resolutionDate(parseDateTime(fields.resolutionDate()))
                    .creator(creator)
                    .reporter(reporter)
                    .assignee(assignee)
                    .build();

        } catch (Exception e) {
            log.error("Failed to map issue: {}", jiraIssue.key(), e);
            throw new RuntimeException("Issue mapping failed: " + jiraIssue.key(), e);
        }
    }

    private Integer parseInteger(String value) {
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

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }

        try {
            // 전체 DateTime 형식 시도
            return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Date만 있는 형식 시도 (duedate 등)
                return LocalDate.parse(dateTimeString, DATE_FORMATTER).atStartOfDay();
            } catch (DateTimeParseException e2) {
                log.warn("Failed to parse datetime: {}", dateTimeString);
                return null;
            }
        }
    }

    private JiraProject findJiraProject(IssueMetadataApiResponse.Fields fields) {
        if (fields.project() != null && fields.project().projectId() != null) {
            Integer projectId = parseInteger(fields.project().projectId());
            JiraProject jiraProject = jiraProjectRepository.findById(projectId).orElse(null);
            if (jiraProject == null) {
                log.warn("Project not found: {}", projectId);
            }
            return jiraProject;
        }
        return null;
    }

    private JiraUser findUser(IssueMetadataApiResponse.UserID userID) {
        if (userID != null && userID.id() != null) {
            return jiraUserRepository.findById(userID.id()).orElse(null);
        }
        return null;
    }

    private IssueType findIssueType(IssueMetadataApiResponse.Fields fields) {
        if (fields.issueType() != null && fields.issueType().id() != null) {
            Integer issueTypeId = parseInteger(fields.issueType().id());
            IssueType issueType = issueTypeRepository.findById(issueTypeId).orElse(null);
            if (issueType == null) {
                log.warn("IssueType not found: {}", issueTypeId);
            }
            return issueType;
        }
        return null;
    }
}