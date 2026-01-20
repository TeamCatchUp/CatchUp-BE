package com.team.catchup.jira.service;

import com.team.catchup.common.sse.dto.SyncCount;
import com.team.catchup.jira.dto.external.IssueMetadataApiResponse;
import com.team.catchup.jira.dto.external.IssueTypeApiResponse;
import com.team.catchup.jira.dto.external.JiraProjectApiResponse;
import com.team.catchup.jira.dto.external.JiraUserApiResponse;
import com.team.catchup.jira.dto.response.IssueSyncResult;
import com.team.catchup.jira.dto.response.ProjectSyncResult;
import com.team.catchup.jira.entity.*;
import com.team.catchup.jira.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraProcessor {

    private final JiraApiService jiraApiService;
    private final JiraPersistenceService jiraPersistenceService;

    private final JiraProjectMapper jiraProjectMapper;
    private final JiraUserMapper jiraUserMapper;
    private final IssueTypeMapper issueTypeMapper;
    private final IssueMetaDataMapper issueMetaDataMapper;
    private final IssueLinkMapper issueLinkMapper;
    private final IssueAttachmentMapper issueAttachmentMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SyncCount syncProjects() {
        log.info("[JIRA PROCESSOR] Project Sync Started");

        int startAt = 0;
        int maxResults = 100;
        int totalFetched = 0;
        int totalSaved = 0;
        boolean hasMore = true;

        while (hasMore) {
            JiraProjectApiResponse response = jiraApiService
                    .fetchProjects(startAt, maxResults)
                    .block();

            if (response == null || response.values() == null || response.values().isEmpty()) {
                log.warn("[JIRA PROCESSOR] Empty Response");
                break;
            }

            List<JiraProject> entities = response.values().stream()
                    .map(jiraProjectMapper::toEntity)
                    .toList();

            totalFetched += entities.size();
            totalSaved += jiraPersistenceService.saveAllProjects(entities);

            hasMore = !Boolean.TRUE.equals(response.isLast());
            if (hasMore) {
                startAt += maxResults;
            }
        }

        log.info("[JIRA PROCESSOR] Project Sync Completed | Fetched: {}, Saved: {}", totalFetched, totalSaved);
        return SyncCount.of(totalFetched, totalSaved);
    }
    // =================================================================================================================
    public SyncCount syncUsers() {
        log.info("[JIRA PROCESSOR] User Sync Started");

        int startAt = 0;
        int maxResults = 100;
        int totalFetched = 0;
        int totalSaved = 0;
        boolean hasMore = true;

        while (hasMore) {
            List<JiraUserApiResponse> responses = jiraApiService
                    .fetchUsers(startAt, maxResults)
                    .block();

            if (responses == null || responses.isEmpty()) {
                break;
            }

            List<JiraUser> entities = responses.stream()
                    .map(jiraUserMapper::toEntity)
                    .toList();

            totalFetched += entities.size();
            totalSaved += jiraPersistenceService.saveAllUsers(entities);

            if (responses.size() < maxResults) {
                hasMore = false;
            } else {
                startAt += maxResults;
            }
        }

        log.info("[JIRA PROCESSOR] User Sync Completed | Fetched: {}, Saved: {}", totalFetched, totalSaved);
        return SyncCount.of(totalFetched, totalSaved);
    }

    public SyncCount syncIssueTypes() {
        log.info("[JIRA PROCESSOR] IssueType Sync Started");

        List<IssueTypeApiResponse> responses = jiraApiService
                .fetchIssueTypes()
                .block();

        if (responses == null || responses.isEmpty()) {
            log.warn("[JIRA][ISSUE TYPE SYNC] Response is Empty");
            return SyncCount.empty();
        }

        List<IssueType> entities = responses.stream()
                .map(issueTypeMapper::toEntity)
                .toList();

        int totalSaved = jiraPersistenceService.saveAllIssueTypes(entities);

        log.info("[JIRA PROCESSOR] IssueType Sync Completed | Fetched: {}, Saved: {}", entities.size(), totalSaved);
        return SyncCount.of(entities.size(), totalSaved);
    }

    public ProjectSyncResult syncSingleProjectIssue(String projectKey) {
        log.info("[JIRA][PROJECT ISSUE SYNC] projectKey: {}", projectKey);

        try {
            IssueSyncResult issueResult = syncIssuesForProject(projectKey);

            log.info("[JIRA PROCESSOR] Issue Sync Completed | Project Key: {}", projectKey);
            return ProjectSyncResult.success(
                    projectKey,
                    issueResult.issues(),
                    issueResult.issueLinks(),
                    issueResult.attachments()
            );
        } catch (Exception e) {
            log.error("[JIRA PROCESSOR] Project Issue Sync Failed - projectKey: {}", projectKey, e);
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
            IssueMetadataApiResponse response = jiraApiService
                    .fetchIssues(projectKey, nextPageToken, 1000, true)
                    .block();

            if (response == null || response.issues() == null || response.issues().isEmpty()) {
                log.warn("[JIRA PROCESSOR] Empty Response for Project Key: {}", projectKey);
                break;
            }

            // Issue Metadata
            List<IssueMetadata> issueEntities = response.issues().stream()
                    .map(issueMetaDataMapper::toEntity)
                    .toList();
            totalIssuesFetched += issueEntities.size();
            totalIssuesSaved += jiraPersistenceService.saveAllIssues(issueEntities);

            // MeiliSearch Document 변환 및 생성 이벤트 발행
            // applicationEventPublisher.publishEvent(new SyncedIssueMetaDataEvent(response)); (Deprecated. Entity->Document로 변경)

            // IssueLinks & Attachments
            for (IssueMetadataApiResponse.JiraIssue jiraIssue : response.issues()) {
                // Issue Links
                if (jiraIssue.fields().issueLinks() != null) {
                    for (IssueMetadataApiResponse.IssueLink linkDto : jiraIssue.fields().issueLinks()) {
                        totalLinksFetched++;
                        if (processIssueLink(linkDto)) {
                            totalLinksSaved++;
                        }
                    }
                }

                // Attachments
                if (jiraIssue.fields().attachments() != null) {
                    Integer issueId = Integer.parseInt(jiraIssue.id());
                    for (IssueMetadataApiResponse.IssueAttachment attachmentDto : jiraIssue.fields().attachments()) {
                        totalAttachmentsFetched++;
                        if (processAttachment(attachmentDto, issueId)) {
                            totalAttachmentsSaved++;
                        }
                    }
                }
            }

            hasMore = !Boolean.TRUE.equals(response.isLast());
            nextPageToken = response.nextPageToken();
        }

        return IssueSyncResult.of(
                SyncCount.of(totalIssuesFetched, totalIssuesSaved),
                SyncCount.of(totalLinksFetched, totalLinksSaved),
                SyncCount.of(totalAttachmentsFetched, totalAttachmentsSaved)
        );
    }

    private boolean processIssueLink(IssueMetadataApiResponse.IssueLink linkDto) {
        try {
            Integer linkTypeId = Integer.parseInt(linkDto.type().id());

            // Link Type 저장
            IssueLinkType linkType = issueLinkMapper.linkTypeToEntity(linkDto.type());
            jiraPersistenceService.saveLinkTypeIfNotExists(linkType);

            Integer linkId = Integer.parseInt(linkDto.id());
            if (jiraPersistenceService.existsIssueLinkById(linkId)) {
                return updateExistingIssueLink(linkDto, linkId, linkTypeId);
            } else {
                IssueLink issueLink = issueLinkMapper.issueLinkToEntity(linkDto);
                return jiraPersistenceService.saveIssueLinkIfNotExists(issueLink);
            }
        } catch (Exception e) {
            log.error("[JIRA][ISSUE LINK] Failed Saving - linkId: {}, error: {}", linkDto.id(), e.getMessage());
            return false;
        }
    }

    private boolean updateExistingIssueLink(IssueMetadataApiResponse.IssueLink linkDto,
                                            Integer linkId, Integer linkTypeId) {
        IssueLink existingLink = jiraPersistenceService.findIssueLinkById(linkId).orElse(null);
        if (existingLink == null) {
            return false;
        }

        IssueMetadata finalInward = existingLink.getInwardIssue();
        IssueMetadata finalOutward = existingLink.getOutwardIssue();

        if (linkDto.inwardIssue() != null && finalInward == null) {
            Integer inwardId = Integer.parseInt(linkDto.inwardIssue().id());
            finalInward = jiraPersistenceService.findIssueById(inwardId).orElse(null);
        }

        if (linkDto.outwardIssue() != null && finalOutward == null) {
            Integer outwardId = Integer.parseInt(linkDto.outwardIssue().id());
            finalOutward = jiraPersistenceService.findIssueById(outwardId).orElse(null);
        }

        IssueLinkType linkType = jiraPersistenceService.findLinkTypeById(linkTypeId).orElse(null);
        if (linkType == null) {
            return false;
        }

        IssueLink updatedLink = IssueLink.builder()
                .linkId(linkId)
                .inwardIssue(finalInward)
                .outwardIssue(finalOutward)
                .linkType(linkType)
                .build();

        return jiraPersistenceService.updateIssueLink(updatedLink);
    }

    private boolean processAttachment(IssueMetadataApiResponse.IssueAttachment attachmentDto, Integer issueId) {
        try {
            IssueAttachment attachment = issueAttachmentMapper.toEntity(attachmentDto, issueId);
            return jiraPersistenceService.saveAttachmentIfNotExists(attachment);
        } catch (Exception e) {
            log.error("[JIRA][ATTACHMENT] Failed Saving - attachmentId: {}, error: {}", attachmentDto.id(), e.getMessage());
            return false;
        }
    }
}