package com.team.catchup.jira.dto.response;

import com.team.catchup.jira.entity.IssueAttachment;
import com.team.catchup.jira.entity.IssueMetadata;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        LocalDateTime resolutionDate,

        String assignee,

        Map<String, String> attachments

) {
    public static JiraIssueResponse of(IssueMetadata entity,
                                       List<String> parentSummaries,
                                       List<String> childrenSummaries) {

        Map<String, String> attachmentMap = entity.getAttachments().stream()
                .collect(Collectors.toMap(
                        IssueAttachment::getFileName,      // Key: 파일명
                        IssueAttachment::getDownloadUrl,   // Value: 다운로드 URL
                        (oldValue, newValue) -> newValue
                ));

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
                .assignee(entity.getAssignee() != null ? entity.getAssignee().getDisplayName() : null)
                .attachments(attachmentMap)
                .build();
    }
}
