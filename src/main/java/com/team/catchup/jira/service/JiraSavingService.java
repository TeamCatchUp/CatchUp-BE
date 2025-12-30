package com.team.catchup.jira.service;

import com.team.catchup.jira.entity.*;
import com.team.catchup.jira.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ==================== Project ====================

    // 배치 저장
    public int saveAllProjects(List<JiraProject> entities) {
        int savedCount = 0;
        for (JiraProject entity : entities) {
            if (saveProjectIfNotExists(entity)) {
                savedCount++;
            }
        }
        log.info("[JIRA SAVING] Projects saved - saved: {}, total: {}", savedCount, entities.size());
        return savedCount;
    }

    // 단건 저장
    private boolean saveProjectIfNotExists(JiraProject entity) {
        if (jiraProjectRepository.existsById(entity.getProjectId())) {
            log.debug("[JIRA SAVING] Project already exists - id: {}", entity.getProjectId());
            return false;
        }
        jiraProjectRepository.save(entity);
        log.debug("[JIRA SAVING] Project saved - key: {}", entity.getProjectKey());
        return true;
    }

    // ==================== User ====================

    // 배치 저장
    public int saveAllUsers(List<JiraUser> entities) {
        int savedCount = 0;
        for (JiraUser entity : entities) {
            if (saveUserIfNotExists(entity)) {
                savedCount++;
            }
        }
        log.info("[JIRA SAVING] Users saved - saved: {}, total: {}", savedCount, entities.size());
        return savedCount;
    }

    // 단건 저장
    private boolean saveUserIfNotExists(JiraUser entity) {
        if (jiraUserRepository.existsById(entity.getAccountId())) {
            log.debug("[JIRA SAVING] User already exists - accountId: {}", entity.getAccountId());
            return false;
        }
        jiraUserRepository.save(entity);
        log.debug("[JIRA SAVING] User saved - accountId: {}", entity.getAccountId());
        return true;
    }

    // ==================== Issue Type ====================

    // 배치 저장
    public int saveAllIssueTypes(List<IssueType> entities) {
        int savedCount = 0;
        for (IssueType entity : entities) {
            if (saveIssueTypeIfNotExists(entity)) {
                savedCount++;
            }
        }
        log.info("[JIRA SAVING] IssueTypes saved - saved: {}, total: {}", savedCount, entities.size());
        return savedCount;
    }

    // 단건 저장
    private boolean saveIssueTypeIfNotExists(IssueType entity) {
        if (issueTypeRepository.existsById(entity.getId())) {
            log.debug("[JIRA SAVING] IssueType already exists - id: {}", entity.getId());
            return false;
        }
        issueTypeRepository.save(entity);
        log.debug("[JIRA SAVING] IssueType saved - id: {}", entity.getId());
        return true;
    }

    // ==================== Issue Metadata ====================

    // 배치 저장
    public int saveAllIssues(List<IssueMetadata> entities) {
        int savedCount = 0;
        for (IssueMetadata entity : entities) {
            if (saveIssueIfNotExists(entity)) {
                savedCount++;
            }
        }
        log.info("[JIRA SAVING] Issues saved - saved: {}, total: {}", savedCount, entities.size());
        return savedCount;
    }

    // 단건 저장
    private boolean saveIssueIfNotExists(IssueMetadata entity) {
        if (issueMetaDataRepository.existsByIssueKey(entity.getIssueKey())) {
            log.debug("[JIRA SAVING] Issue already exists - key: {}", entity.getIssueKey());
            return false;
        }
        issueMetaDataRepository.save(entity);
        log.debug("[JIRA SAVING] Issue saved - key: {}", entity.getIssueKey());
        return true;
    }

    // ==================== Issue Link Type ====================

    // 단건 저장
    public boolean saveLinkTypeIfNotExists(IssueLinkType entity) {
        if (issueLinkTypeRepository.existsById(entity.getLinkTypeId())) {
            log.debug("[JIRA SAVING] LinkType already exists - id: {}", entity.getLinkTypeId());
            return false;
        }
        issueLinkTypeRepository.save(entity);
        log.debug("[JIRA SAVING] LinkType saved - id: {}", entity.getLinkTypeId());
        return true;
    }

    // ==================== Issue Link ====================

    // 단건 저장
    public boolean saveIssueLinkIfNotExists(IssueLink entity) {
        if (issueLinkRepository.existsById(entity.getLinkId())) {
            log.debug("[JIRA SAVING] IssueLink already exists - id: {}", entity.getLinkId());
            return false;
        }
        issueLinkRepository.save(entity);
        log.debug("[JIRA SAVING] IssueLink saved - id: {}", entity.getLinkId());
        return true;
    }

    // 업데이트
    public boolean updateIssueLink(IssueLink entity) {
        Optional<IssueLink> existingLink = issueLinkRepository.findById(entity.getLinkId());
        if (existingLink.isEmpty()) {
            log.debug("[JIRA SAVING] IssueLink not found for update - id: {}", entity.getLinkId());
            return false;
        }
        issueLinkRepository.save(entity);
        log.debug("[JIRA SAVING] IssueLink updated - id: {}", entity.getLinkId());
        return true;
    }

    // 존재 확인
    public boolean existsIssueLinkById(Integer linkId) {
        return issueLinkRepository.existsById(linkId);
    }

    // ==================== Attachments ====================

    // 단건 저장
    public boolean saveAttachmentIfNotExists(IssueAttachment entity) {
        if (issueAttachmentRepository.existsById(entity.getId())) {
            log.debug("[JIRA SAVING] Attachment already exists - id: {}", entity.getId());
            return false;
        }
        issueAttachmentRepository.save(entity);
        log.debug("[JIRA SAVING] Attachment saved - id: {}", entity.getId());
        return true;
    }

    // ==================== 조회 ====================

    public Optional<IssueLink> findIssueLinkById(Integer linkId) {
        return issueLinkRepository.findById(linkId);
    }

    public Optional<IssueMetadata> findIssueById(Integer issueId) {
        return issueMetaDataRepository.findById(issueId);
    }

    public Optional<IssueLinkType> findLinkTypeById(Integer linkTypeId) {
        return issueLinkTypeRepository.findById(linkTypeId);
    }
}