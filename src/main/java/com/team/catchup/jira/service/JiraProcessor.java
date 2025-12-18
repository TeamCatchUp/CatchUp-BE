package com.team.catchup.jira.service;

import com.team.catchup.common.sse.dto.SyncCount;
import com.team.catchup.jira.dto.IssueSyncResult;
import com.team.catchup.jira.dto.response.*;
import com.team.catchup.jira.entity.*;
import com.team.catchup.jira.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraProcessor {

    private final JiraApiService jiraApiService;
    private final JiraSavingService jiraSavingService;

    private final JiraProjectMapper jiraProjectMapper;
    private final JiraUserMapper jiraUserMapper;
    private final IssueTypeMapper issueTypeMapper;
    private final IssueMetaDataMapper issueMetaDataMapper;
    private final IssueLinkMapper issueLinkMapper;
    private final IssueAttachmentMapper issueAttachmentMapper;

    public SyncCount syncProjects() {
        log.info("[JIRA][PROJECT SYNC] STARTED");

        int startAt = 0;
        int maxResults = 100;
        int totalFetched = 0;
        int totalSaved = 0;
        boolean hasMore = true;

        while (hasMore) {
            JiraProjectResponse response = jiraApiService
                    .fetchProjects(startAt, maxResults)
                    .block();

            if (response == null || response.values() == null || response.values().isEmpty()) {
                break;
            }

            List<JiraProject> entities = response.values().stream()
                    .map(jiraProjectMapper::toEntity)
                    .toList();

            totalFetched += entities.size();
            totalSaved += jiraSavingService.saveAllProjectsIfNotExists(entities);

            hasMore = !Boolean.TRUE.equals(response.isLast());
            if (hasMore) {
                startAt += maxResults;
            }
        }

        log.info("[JIRA][PROJECT SYNC] SUCCESS | Fetched: {}, Saved: {}", totalFetched, totalSaved);
        return SyncCount.of(totalFetched, totalSaved);
    }

    public SyncCount syncUsers() {
        log.info("[JIRA][USER SYNC] STARTED");

        int startAt = 0;
        int maxResults = 100;
        int totalFetched = 0;
        int totalSaved = 0;
        boolean hasMore = true;

        while (hasMore) {
            List<JiraUserResponse> responses = jiraApiService
                    .fetchUsers(startAt, maxResults)
                    .block();

            if (responses == null || responses.isEmpty()) {
                break;
            }

            List<JiraUser> entities = responses.stream()
                    .map(jiraUserMapper::toEntity)
                    .toList();

            totalFetched += entities.size();
            totalSaved += jiraSavingService.saveAllUsersIfNotExists(entities);

            if (responses.size() < maxResults) {
                hasMore = false;
            } else {
                startAt += maxResults;
            }
        }

        log.info("[JIRA][USER SYNC] SUCCESS | Fetched: {}, Saved: {}", totalFetched, totalSaved);
        return SyncCount.of(totalFetched, totalSaved);
    }

    public SyncCount syncIssueTypes() {
        log.info("[JIRA][ISSUE TYPE SYNC] STARTED");

        List<IssueTypeResponse> responses = jiraApiService
                .fetchIssueTypes()
                .block();

        if (responses == null || responses.isEmpty()) {
            log.warn("[JIRA][ISSUE TYPE SYNC] Response is Empty");
            return SyncCount.empty();
        }

        List<IssueType> entities = responses.stream()
                .map(issueTypeMapper::toEntity)
                .toList();

        int totalSaved = jiraSavingService.saveAllIssueTypesIfNotExists(entities);

        log.info("[JIRA][ISSUE TYPE SYNC] SUCCESS | Fetched: {}, Saved: {}", entities.size(), totalSaved);
        return SyncCount.of(entities.size(), totalSaved);
    }

    public ProjectSyncResult syncSingleProjectIssue(String projectKey) {
        log.info("[JIRA][PROJECT ISSUE SYNC] projectKey: {}", projectKey);

        try {
            IssueSyncResult issueResult = syncIssuesForProject(projectKey);

            log.info("[JIRA][PROJECT ISSUE SYNC] SUCCESS | projectKey: {}", projectKey);
            return ProjectSyncResult.success(
                    projectKey,
                    issueResult.getIssues(),
                    issueResult.getIssueLinks(),
                    issueResult.getAttachments()
            );
        } catch (Exception e) {
            log.error("[JIRA][PROJECT ISSUE SYNC] FAILED | projectKey: {}", projectKey, e);
            return ProjectSyncResult.failure(projectKey, e.getMessage());
        }
    }

    //==================================================================================================================

    private IssueSyncResult syncIssuesForProject(String projectKey) {
        String nextPageToken = null;
        boolean hasMore = true;

        int totalIssuesFetched = 0;
        int totalIssuesSaved = 0;
        int totalLinksFetched = 0;
        int totalLinksSaved = 0;
        int totalAttachmentsFetched = 0;
        int totalAttachmentsSaved = 0;

        while (hasMore) {
            IssueMetaDataResponse response = jiraApiService
                    .fetchIssues(projectKey, nextPageToken, 1000, true)
                    .block();

            if (response == null || response.issues() == null || response.issues().isEmpty()) {
                break;
            }

            // Issue Metadata
            List<IssueMetadata> issueEntities = response.issues().stream()
                    .map(issueMetaDataMapper::toEntity)
                    .toList();
            totalIssuesFetched += issueEntities.size();
            totalIssuesSaved += jiraSavingService.saveAllIssuesIfNotExists(issueEntities);

            // IssueLinks & Attachments
            for (IssueMetaDataResponse.JiraIssue jiraIssue : response.issues()) {
                // Issue Links
                if (jiraIssue.fields().issueLinks() != null) {
                    for (IssueMetaDataResponse.IssueLink linkDto : jiraIssue.fields().issueLinks()) {
                        totalLinksFetched++;
                        if (saveIssueLink(linkDto)) {
                            totalLinksSaved++;
                        }
                    }
                }

                // Attachments
                if (jiraIssue.fields().attachments() != null) {
                    Integer issueId = Integer.parseInt(jiraIssue.id());
                    for (IssueMetaDataResponse.IssueAttachment attachmentDto : jiraIssue.fields().attachments()) {
                        totalAttachmentsFetched++;
                        if (saveAttachment(attachmentDto, issueId)) {
                            totalAttachmentsSaved++;
                        }
                    }
                }
            }

            hasMore = !Boolean.TRUE.equals(response.isLast());
            nextPageToken = response.nextPageToken();
        }

        return IssueSyncResult.builder()
                .issues(SyncCount.of(totalIssuesFetched, totalIssuesSaved))
                .issueLinks(SyncCount.of(totalLinksFetched, totalLinksSaved))
                .attachments(SyncCount.of(totalAttachmentsFetched, totalAttachmentsSaved))
                .build();
    }

    private boolean saveIssueLink(IssueMetaDataResponse.IssueLink linkDto) {
        try {
            Integer linkTypeId = Integer.parseInt(linkDto.type().id());

            // Link Type 저장
            IssueLinkType linkType = issueLinkMapper.linkTypeToEntity(linkDto.type());
            jiraSavingService.saveLinkTypeIfNotExists(linkType);

            Integer linkId = Integer.parseInt(linkDto.id());
            if (jiraSavingService.existsIssueLinkById(linkId)) {
                return updateExistingIssueLink(linkDto, linkId, linkTypeId);
            } else {
                IssueLink issueLink = issueLinkMapper.issueLinkToEntity(linkDto);
                return jiraSavingService.saveIssueLinkIfNotExists(issueLink);
            }
        } catch (Exception e) {
            log.error("[JIRA][ISSUE LINK] Failed Saving - linkId: {}, error: {}", linkDto.id(), e.getMessage());
            return false;
        }
    }

    private boolean updateExistingIssueLink(IssueMetaDataResponse.IssueLink linkDto,
                                            Integer linkId, Integer linkTypeId) {
        IssueLink existingLink = jiraSavingService.findIssueLinkById(linkId).orElse(null);
        if (existingLink == null) {
            return false;
        }

        IssueMetadata finalInward = existingLink.getInwardIssue();
        IssueMetadata finalOutward = existingLink.getOutwardIssue();

        if (linkDto.inwardIssue() != null && finalInward == null) {
            Integer inwardId = Integer.parseInt(linkDto.inwardIssue().id());
            finalInward = jiraSavingService.findIssueById(inwardId).orElse(null);
        }

        if (linkDto.outwardIssue() != null && finalOutward == null) {
            Integer outwardId = Integer.parseInt(linkDto.outwardIssue().id());
            finalOutward = jiraSavingService.findIssueById(outwardId).orElse(null);
        }

        IssueLinkType linkType = jiraSavingService.findLinkTypeById(linkTypeId).orElse(null);
        if (linkType == null) {
            return false;
        }

        IssueLink updatedLink = IssueLink.builder()
                .linkId(linkId)
                .inwardIssue(finalInward)
                .outwardIssue(finalOutward)
                .linkType(linkType)
                .build();

        return jiraSavingService.updateIssueLink(updatedLink);
    }

    private boolean saveAttachment(IssueMetaDataResponse.IssueAttachment attachmentDto, Integer issueId) {
        try {
            IssueAttachment attachment = issueAttachmentMapper.toEntity(attachmentDto, issueId);
            return jiraSavingService.saveAttachmentIfNotExists(attachment);
        } catch (Exception e) {
            log.error("[JIRA][ATTACHMENT] Failed Saving - attachmentId: {}, error: {}", attachmentDto.id(), e.getMessage());
            return false;
        }
    }
}