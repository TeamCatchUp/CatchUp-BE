package com.team.catchup.jira.service;

import com.team.catchup.jira.entity.*;
import com.team.catchup.jira.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JiraSavingService {

    private final IssueMetaDataRepository issueMetaDataRepository;
    private final IssueLinkRepository issueLinkRepository;
    private final IssueLinkTypeRepository issueLinkTypeRepository;
    private final IssueAttachmentRepository issueAttachmentRepository;
    private final IssueTypeRepository issueTypeRepository;
    private final JiraUserRepository jiraUserRepository;
    private final JiraProjectRepository jiraProjectRepository;

    // ==================== 단건 저장 ====================

    public boolean saveLinkTypeIfNotExists(IssueLinkType entity) {
        if (issueLinkTypeRepository.existsById(entity.getLinkTypeId())) {
            log.debug("[SKIP] 이미 존재하는 LinkType: {}", entity.getLinkTypeId());
            return false;
        }
        issueLinkTypeRepository.save(entity);
        log.debug("[SAVE] LinkType 저장 완료: {}", entity.getLinkTypeId());
        return true;
    }

    public boolean saveIssueLinkIfNotExists(IssueLink entity) {
        if (issueLinkRepository.existsById(entity.getLinkId())) {
            log.debug("[SKIP] 이미 존재하는 IssueLink: {}", entity.getLinkId());
            return false;
        }
        issueLinkRepository.save(entity);
        log.debug("[SAVE] IssueLink 저장 완료: {}", entity.getLinkId());
        return true;
    }

    public boolean saveAttachmentIfNotExists(IssueAttachment entity) {
        if (issueAttachmentRepository.existsById(entity.getId())) {
            log.debug("[SKIP] 이미 존재하는 Attachment: {}", entity.getId());
            return false;
        }
        issueAttachmentRepository.save(entity);
        log.debug("[SAVE] Attachment 저장 완료: {}", entity.getId());
        return true;
    }

    // ==================== 업데이트 ====================

    public boolean updateIssueLink(IssueLink entity) {
        Optional<IssueLink> existingLink = issueLinkRepository.findById(entity.getLinkId());
        if (existingLink.isEmpty()) {
            log.debug("[SKIP] 존재하지 않는 IssueLink: {}", entity.getLinkId());
            return false;
        }
        issueLinkRepository.save(entity);
        log.debug("[UPDATE] IssueLink 업데이트 완료: {}", entity.getLinkId());
        return true;
    }

    // ==================== 조회 (업데이트용) ====================

    public Optional<IssueLink> findIssueLinkById(Integer linkId) {
        return issueLinkRepository.findById(linkId);
    }

    public Optional<IssueMetadata> findIssueById(Integer issueId) {
        return issueMetaDataRepository.findById(issueId);
    }

    public Optional<IssueLinkType> findLinkTypeById(Integer linkTypeId) {
        return issueLinkTypeRepository.findById(linkTypeId);
    }

    public boolean existsIssueLinkById(Integer linkId) {
        return issueLinkRepository.existsById(linkId);
    }

    // ==================== 배치 저장 ====================

    public int saveAllIssuesIfNotExists(List<IssueMetadata> entities) {
        int savedCount = 0;
        for (IssueMetadata entity : entities) {
            if (issueMetaDataRepository.existsByIssueKey(entity.getIssueKey())) {
                log.debug("[SKIP] 이미 존재하는 이슈: {}", entity.getIssueKey());
                continue;
            }
            issueMetaDataRepository.save(entity);
            savedCount++;
        }
        log.info("[BATCH SAVE] Issue 저장 완료 - Saved: {}, Total: {}", savedCount, entities.size());
        return savedCount;
    }

    public int saveAllIssueTypesIfNotExists(List<IssueType> entities) {
        int savedCount = 0;
        for (IssueType entity : entities) {
            if (issueTypeRepository.existsById(entity.getId())) {
                log.debug("[SKIP] 이미 존재하는 IssueType: {}", entity.getId());
                continue;
            }
            issueTypeRepository.save(entity);
            savedCount++;
        }
        log.info("[BATCH SAVE] IssueType 저장 완료 - Saved: {}, Total: {}", savedCount, entities.size());
        return savedCount;
    }

    public int saveAllUsersIfNotExists(List<JiraUser> entities) {
        int savedCount = 0;
        for (JiraUser entity : entities) {
            if (jiraUserRepository.existsById(entity.getAccountId())) {
                log.debug("[SKIP] 이미 존재하는 User: {}", entity.getAccountId());
                continue;
            }
            jiraUserRepository.save(entity);
            savedCount++;
        }
        log.info("[BATCH SAVE] User 저장 완료 - Saved: {}, Total: {}", savedCount, entities.size());
        return savedCount;
    }

    public int saveAllProjectsIfNotExists(List<JiraProject> entities) {
        int savedCount = 0;
        for (JiraProject entity : entities) {
            if (jiraProjectRepository.existsById(entity.getProjectId())) {
                log.debug("[SKIP] 이미 존재하는 Project: {}", entity.getProjectKey());
                continue;
            }
            jiraProjectRepository.save(entity);
            log.debug("[SAVE] Project 저장 완료: {}", entity.getProjectKey());
            savedCount++;
        }
        log.info("[BATCH SAVE] Project 저장 완료 - Saved: {}, Total: {}", savedCount, entities.size());
        return savedCount;
    }
}
