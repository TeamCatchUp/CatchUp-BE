package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.response.JiraProjectResponse;
import com.team.catchup.jira.entity.JiraProject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class JiraProjectMapper {

    private static final DateTimeFormatter JIRA_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public JiraProject toEntity(JiraProjectResponse.ProjectValue projectValue) {
        try{
            String avatarUrl = null;
            if (projectValue.avatarUrls() != null) {
                avatarUrl = projectValue.avatarUrls().avatar48();
            }

            Integer totalIssueCount = null;
            LocalDateTime lastIssueUpdateTime = null;
            if(projectValue.insight() != null) {
                totalIssueCount = projectValue.insight().totalIssueCount();
                lastIssueUpdateTime = parseDateTimeSafely(
                        projectValue.insight().lastIssueUpdateTime()
                );
            }

            return JiraProject.builder()
                    .projectId(Integer.parseInt(projectValue.id()))
                    .projectKey(projectValue.key())
                    .name(projectValue.name())
                    .description(projectValue.description())
                    .projectTypeKey(projectValue.projectTypeKey())
                    .style(projectValue.style())
                    .simplified(projectValue.simplified())
                    .isPrivate(projectValue.isPrivate())
                    .avatarUrl(avatarUrl)
                    .totalIssueCount(totalIssueCount)
                    .lastIssueUpdateTime(lastIssueUpdateTime)
                    .build();

        } catch (Exception e) {
            log.error("Failed to map Project: {}", projectValue.key(), e);
            throw new RuntimeException("Project mapping failed: " + projectValue.key(), e);
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
