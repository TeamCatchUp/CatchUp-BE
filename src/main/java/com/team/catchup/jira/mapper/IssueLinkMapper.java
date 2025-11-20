package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import com.team.catchup.jira.entity.IssueLink;
import com.team.catchup.jira.entity.IssueLinkType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IssueLinkMapper {

    /**
     * LinkType DTO -> Entity
     */
    public IssueLinkType linkTypeToEntity(IssueMetaDataResponse.LinkType linkType) {
        try {
            return IssueLinkType.builder()
                    .linkTypeId(Integer.parseInt(linkType.id()))
                    .name(linkType.name())
                    .inward(linkType.inward())
                    .outward(linkType.outward())
                    .selfUrl(linkType.self())
                    .build();
        } catch (Exception e) {
            log.error("Failed to map LinkType: {}", linkType.id(), e);
            throw new RuntimeException("LinkType mapping failed", e);
        }
    }

    /**
     * IssueLink DTO -> Entity
     */
    public IssueLink issueLinkToEntity(IssueMetaDataResponse.IssueLink issueLink) {
        try {
            Integer inwardIssueId = null;
            Integer outwardIssueId = null;

            if (issueLink.inwardIssue() != null) {
                inwardIssueId = Integer.parseInt(issueLink.inwardIssue().id());
            }

            if (issueLink.outwardIssue() != null) {
                outwardIssueId = Integer.parseInt(issueLink.outwardIssue().id());
            }

            return IssueLink.builder()
                    .linkId(Integer.parseInt(issueLink.id()))
                    .inwardIssueId(inwardIssueId)
                    .outwardIssueId(outwardIssueId)
                    .linkTypeId(Integer.parseInt(issueLink.type().id()))
                    .build();

        } catch (Exception e) {
            log.error("Failed to map IssueLink: {}", issueLink.id(), e);
            throw new RuntimeException("IssueLink mapping failed", e);
        }
    }
}