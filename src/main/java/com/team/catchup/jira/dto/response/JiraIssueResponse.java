package com.team.catchup.jira.dto.response;

import com.team.catchup.jira.entity.IssueMetadata;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record JiraIssueResponse(
        Integer issueId,

        String issueKey,

        String issueUrl,

        String issueTypeName,

        String projectName,

        String summary,

        List<String> parentIssueSummaries,

        List<String> childrenIssueSummaries,

        LocalDateTime resolutionDate
) {
    public static JiraIssueResponse of(IssueMetadata entity,
                                       List<String> parentSummaries,
                                       List<String> childrenSummaries) {
        return JiraIssueResponse.builder()
                .issueId(entity.getIssueId())
                .issueKey(entity.getIssueKey())
                .issueUrl(entity.getSelf())
                .issueTypeName(entity.getIssueType() != null ? entity.getIssueType().getName() : null)
                .projectName(entity.getProject() != null ? entity.getProject().getName() : null)
                .summary(entity.getSummary())
                .parentIssueSummaries(parentSummaries)
                .childrenIssueSummaries(childrenSummaries)
                .resolutionDate(entity.getResolutionDate())
                .build();
    }
}
