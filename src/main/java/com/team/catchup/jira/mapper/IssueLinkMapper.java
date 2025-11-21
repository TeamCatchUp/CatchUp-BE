package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import com.team.catchup.jira.entity.IssueLink;
import com.team.catchup.jira.entity.IssueLinkType;
import com.team.catchup.jira.entity.IssueMetadata;
import com.team.catchup.jira.repository.IssueLinkTypeRepository;
import com.team.catchup.jira.repository.IssueMetaDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IssueLinkMapper {

    // ✅ Repository 주입 (연관관계 조회용)
    private final IssueLinkTypeRepository issueLinkTypeRepository;
    private final IssueMetaDataRepository issueMetaDataRepository;

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


    public IssueLink issueLinkToEntity(IssueMetaDataResponse.IssueLink issueLink) {
        try {
            IssueMetadata inwardIssue = null;
            if (issueLink.inwardIssue() != null) {
                Integer inwardIssueId = Integer.parseInt(issueLink.inwardIssue().id());
                inwardIssue = issueMetaDataRepository.findById(inwardIssueId).orElse(null);
                if (inwardIssue == null) {
                    log.warn("Inward Issue not found: {}", inwardIssueId);
                }
            }

            IssueMetadata outwardIssue = null;
            if (issueLink.outwardIssue() != null) {
                Integer outwardIssueId = Integer.parseInt(issueLink.outwardIssue().id());
                outwardIssue = issueMetaDataRepository.findById(outwardIssueId).orElse(null);
                if (outwardIssue == null) {
                    log.warn("Outward Issue not found: {}", outwardIssueId);
                }
            }

            Integer linkTypeId = Integer.parseInt(issueLink.type().id());
            IssueLinkType linkType = issueLinkTypeRepository.findById(linkTypeId)
                    .orElseThrow(() -> new RuntimeException("LinkType not found: " + linkTypeId));

            return IssueLink.builder()
                    .linkId(Integer.parseInt(issueLink.id()))
                    .inwardIssue(inwardIssue)
                    .outwardIssue(outwardIssue)
                    .linkType(linkType)
                    .build();

        } catch (Exception e) {
            log.error("Failed to map IssueLink: {}", issueLink.id(), e);
            throw new RuntimeException("IssueLink mapping failed", e);
        }
    }
}