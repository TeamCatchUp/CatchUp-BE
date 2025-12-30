package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.external.IssueMetadataApiResponse;
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

    private final IssueLinkTypeRepository issueLinkTypeRepository;
    private final IssueMetaDataRepository issueMetaDataRepository;

    public IssueLinkType linkTypeToEntity(IssueMetadataApiResponse.LinkType linkType) {
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


    public IssueLink issueLinkToEntity(IssueMetadataApiResponse.IssueLink issueLink) {
        try {
            IssueMetadata inwardIssue = findIssue(issueLink.inwardIssue());
            IssueMetadata outwardIssue = findIssue(issueLink.outwardIssue());

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

    private IssueMetadata findIssue(IssueMetadataApiResponse.LinkedIssue linkedIssue) {
        if(linkedIssue == null) {
            return null;
        }

        Integer issueId = Integer.parseInt(linkedIssue.id());
        IssueMetadata issue = issueMetaDataRepository.findById(issueId).orElse(null);

        if(issue == null) {
            log.warn("Issue not found: {}", issueId);
        }

        return issue;
    }
}