package com.team.catchup.jira.service;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import com.team.catchup.jira.dto.response.IssueTypeResponse;
import com.team.catchup.jira.dto.response.JiraProjectResponse;
import com.team.catchup.jira.dto.response.JiraUserResponse;
import com.team.catchup.jira.entity.*;
import com.team.catchup.jira.mapper.*;
import com.team.catchup.jira.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraSyncService {

    private final JiraApiService jiraApiService;
    private final IssueMetaDataRepository issueMetaDataRepository;
    private final IssueMetaDataMapper issueMetaDataMapper;
    private final IssueTypeRepository issueTypeRepository;
    private final IssueTypeMapper issueTypeMapper;
    private final JiraUserRepository jiraUserRepository;
    private final JiraUserMapper jiraUserMapper;
    private final IssueLinkRepository issueLinkRepository;
    private final IssueLinkTypeRepository issueLinkTypeRepository;
    private final IssueLinkMapper issueLinkMapper;
    private final IssueAttachmentRepository issueAttachmentRepository;
    private final IssueAttachmentMapper issueAttachmentMapper;
    private final JiraProjectRepository jiraProjectRepository;
    private final JiraProjectMapper jiraProjectMapper;

    /**
     * 초기 전체 동기화 (Full Sync)
     */
    @Transactional
    public int fullSync(String projectKey, Integer maxResults) {
        log.info("[JIRA][FULL SYNC 시작] Project: {}, MaxResults: {}", projectKey, maxResults);

        String nextPageToken = null;
        int totalSaved = 0;
        int totalSkipped = 0;
        boolean hasMore = true;
        int pageCount = 0;
        int issuesWithLinks = 0;
        int issueWithAttachments = 0;
        int issueCount = getTotalIssueCount(projectKey);

        while (hasMore) {
            try {
                pageCount++;

                IssueMetaDataResponse response = jiraApiService
                        .fetchIssuesWithToken(projectKey, nextPageToken, maxResults, true)
                        .block();

                if (response == null || response.issues() == null || response.issues().isEmpty()) {
                    log.info("[JIRA][FULL SYNC] 더 이상 가져올 이슈가 없습니다.");
                    break;
                }

                // Issue Metadata 저장
                List<IssueMetadata> savedIssues = saveIssuesToDB(response.issues());
                totalSaved += savedIssues.size();
                totalSkipped += (response.issues().size() - savedIssues.size());

                // IssueLink & Issue Attachment 저장
                for (IssueMetaDataResponse.JiraIssue jiraIssue : response.issues()) {
                    log.debug("Issue {} - issueLinks: {}",
                            jiraIssue.key(),
                            jiraIssue.fields().issueLinks() != null ?
                                    jiraIssue.fields().issueLinks().size() : "null");

                    // Issue Link 저장
                    if (jiraIssue.fields().issueLinks() != null &&
                            !jiraIssue.fields().issueLinks().isEmpty()) {
                        issuesWithLinks++;
                        saveIssueLinks(jiraIssue.fields().issueLinks());
                    }

                    // Issue Attachment 저장
                    if(jiraIssue.fields().attachments() != null
                            && !jiraIssue.fields().attachments().isEmpty()) {
                        issueWithAttachments++;
                        saveAttachments(
                                Integer.parseInt(jiraIssue.id()),
                                jiraIssue.fields().attachments()
                        );
                    }
                }

                log.info("[JIRA][FULL SYNC 진행중] Page: {}, Fetched: {}, Saved: {}, Skipped: {}",
                        pageCount, response.issues().size(), savedIssues.size(),
                        response.issues().size() - savedIssues.size());

                // 페이지네이션
                hasMore = !Boolean.TRUE.equals(response.isLast());
                nextPageToken = response.nextPageToken();

            } catch (Exception e) {
                log.error("[JIRA][FULL SYNC 실패] Page: {}, Error: {}", pageCount, e.getMessage(), e);
                throw new RuntimeException("Full sync failed at page: " + pageCount, e);
            }
        }

        if(issueCount != (totalSaved + totalSkipped)) {
            throw new IllegalStateException("Saved Issue Count Does Not Match");
        }

        log.info("[JIRA][FULL SYNC 완료] Total Saved: {}, Total Skipped: {}", totalSaved, totalSkipped);

        return totalSaved;
    }
    //==================================================================================================================
    public Integer getTotalIssueCount(String projectKey) {
        String nextPageToken = null;
        boolean hasMore = true;
        int pageCount = 0;
        int totalCount = 0;

        while (hasMore) {
            try {
                pageCount++;

                IssueMetaDataResponse response = jiraApiService
                        .fetchIssuesWithToken(projectKey, nextPageToken, 100, false)
                        .block();

                if (response == null || response.issues() == null || response.issues().isEmpty()) {
                    log.info("[JIRA][Issue Count] 더 이상 가져올 이슈가 없습니다.");
                    break;
                }

                totalCount += response.issues().size();

                log.debug("[JIRA][ISSUE COUNT] Page: {}, Current: {}, Total: {}",
                        pageCount, response.issues().size(), totalCount);


                hasMore = !Boolean.TRUE.equals(response.isLast());
                nextPageToken = response.nextPageToken();

            }catch (Exception e) {
                log.error("[JIRA][Issue Count 실패] Page: {}, Error: {}", pageCount, e.getMessage(), e);
                throw new RuntimeException("Issue Counting failed At page : " + pageCount, e);
            }
        }
        return totalCount;
    }
    private List<IssueMetadata> saveIssuesToDB(List<IssueMetaDataResponse.JiraIssue> jiraIssues) {
        List<IssueMetadata> savedIssues = new ArrayList<>();

        for (IssueMetaDataResponse.JiraIssue jiraIssue : jiraIssues) {
            try {
                // 중복 체크
                if (issueMetaDataRepository.existsByIssueKey(jiraIssue.key())) {
                    log.debug("[JIRA][SKIP] 이미 존재하는 이슈: {}", jiraIssue.key());
                    continue;
                }

                // DTO → Entity 변환
                IssueMetadata issueMetadata = issueMetaDataMapper.toEntity(jiraIssue);

                // DB 저장
                IssueMetadata saved = issueMetaDataRepository.save(issueMetadata);
                savedIssues.add(saved);

                log.debug("[JIRA] 이슈 저장 완료: {}", saved.getIssueKey());

            } catch (Exception e) {
                log.error("[JIRA] 이슈 저장 실패 | Issue: {}, Error: {}", jiraIssue.key(), e.getMessage(), e);
            }
        }
        return savedIssues;
    }
    private void saveIssueLinks(List<IssueMetaDataResponse.IssueLink> issueLinks) {
        if (issueLinks == null || issueLinks.isEmpty()) {
            log.debug("IssueLinks가 null이거나 비어있음");
            return;
        }

        log.info("[JIRA][ISSUE LINK] 저장 시작 - Total: {}", issueLinks.size());

        int savedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;

        for (IssueMetaDataResponse.IssueLink linkDto : issueLinks) {
            try {
                log.debug("처리 중인 Link ID: {}", linkDto.id());

                Integer linkId = Integer.parseInt(linkDto.id());
                Integer linkTypeId = Integer.parseInt(linkDto.type().id());

                if (!issueLinkTypeRepository.existsById(linkTypeId)) {
                    IssueLinkType linkType = issueLinkMapper.linkTypeToEntity(linkDto.type());
                    issueLinkTypeRepository.save(linkType);
                    log.debug("LinkType 저장 완료: {} - {}", linkTypeId, linkDto.type().name());
                }

                if (issueLinkRepository.existsById(linkId)) {
                    // 기존 링크가 있으면 업데이트
                    IssueLink existingLink = issueLinkRepository.findById(linkId)
                            .orElseThrow();

                    IssueMetadata finalInward = existingLink.getInwardIssue();
                    IssueMetadata finalOutward = existingLink.getOutwardIssue();

                    if (linkDto.inwardIssue() != null && finalInward == null) {
                        Integer inwardId = Integer.parseInt(linkDto.inwardIssue().id());
                        finalInward = issueMetaDataRepository.findById(inwardId).orElse(null);
                    }

                    if (linkDto.outwardIssue() != null && finalOutward == null) {
                        Integer outwardId = Integer.parseInt(linkDto.outwardIssue().id());
                        finalOutward = issueMetaDataRepository.findById(outwardId).orElse(null);
                    }

                    IssueLinkType linkType = issueLinkTypeRepository.findById(linkTypeId)
                            .orElseThrow();

                    IssueLink updatedLink = IssueLink.builder()
                            .linkId(linkId)
                            .inwardIssue(finalInward)
                            .outwardIssue(finalOutward)
                            .linkType(linkType)
                            .build();

                    issueLinkRepository.save(updatedLink);
                    updatedCount++;
                    log.debug("IssueLink 업데이트: {}", linkId);

                } else {
                    IssueLink issueLink = issueLinkMapper.issueLinkToEntity(linkDto);
                    issueLinkRepository.save(issueLink);
                    savedCount++;
                    log.debug("IssueLink 저장 완료: {}", linkId);
                }

            } catch (Exception e) {
                skippedCount++;
                log.error("[JIRA] IssueLink 저장 실패 | LinkId: {}, Error: {}",
                        linkDto.id(), e.getMessage(), e);
            }
        }

        log.info("[JIRA][ISSUE LINK] 저장 완료 - Saved: {}, Updated: {}, Skipped: {}",
                savedCount, updatedCount, skippedCount);
    }
    private void saveAttachments(Integer issueId, List<IssueMetaDataResponse.IssueAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        log.debug("[JIRA][ATTACHMENT] 저장 시작 - IssueId: {}, Total: {}", issueId, attachments.size());

        int savedCount = 0;
        int skippedCount = 0;

        for (IssueMetaDataResponse.IssueAttachment attachmentDto : attachments) {
            try {
                Integer attachmentId = Integer.parseInt(attachmentDto.id());

                // 중복 체크
                if (issueAttachmentRepository.existsById(attachmentId)) {
                    log.debug("중복된 Attachment: {}", attachmentId);
                    skippedCount++;
                    continue;
                }

                // Entity 변환 및 저장
                IssueAttachment attachment = issueAttachmentMapper.toEntity(attachmentDto, issueId);
                issueAttachmentRepository.save(attachment);
                savedCount++;

                log.debug("Attachment 저장 완료: {} - {}", attachmentId, attachmentDto.filename());

            } catch (Exception e) {
                skippedCount++;
                log.error("[JIRA] Attachment 저장 실패 | AttachmentId: {}, Error: {}",
                        attachmentDto.id(), e.getMessage());
            }
        }

        log.info("[JIRA][ATTACHMENT] 저장 완료 - IssueId: {}, Saved: {}, Skipped: {}",
                issueId, savedCount, skippedCount);
    }
    // =================================================================================================================
    @Transactional
    public int syncAllIssueTypes() {
        log.info("[JIRA][ISSUE TYPE SYNC 시작]");

        int issueTypeCount = 0;
        try {
            List<IssueTypeResponse> responses = jiraApiService
                    .fetchAllIssueTypes()
                    .block();

            if (responses == null || responses.isEmpty()) {
                log.warn("[JIRA][ISSUE TYPE SYNC] 조회된 IssueType이 없습니다.");
                return 0;
            }

            log.info("[JIRA][ISSUE TYPE SYNC] Fetched: {} IssueTypes", responses.size());

            // DB 저장
            List<IssueType> savedTypes = saveIssueTypesToDB(responses);
            int totalSkipped = responses.size() - savedTypes.size();

            log.info("[JIRA][ISSUE TYPE SYNC 완료] Total: {}, Saved: {}, Skipped: {}",
                    responses.size(), savedTypes.size(), totalSkipped);

            return responses.size();
        } catch (Exception e) {
            log.error("[JIRA][ISSUE TYPE SYNC 실패] Error: {}", e.getMessage(), e);
            throw new RuntimeException("IssueType sync failed", e);
        }
    }
    private List<IssueType> saveIssueTypesToDB(List<IssueTypeResponse> typeResponses) {
        List<IssueType> savedTypes = new ArrayList<>();

        for (IssueTypeResponse response : typeResponses) {
            try {
                // 중복 체크
                Integer issueTypeId = Integer.parseInt(response.id());
                if (issueTypeRepository.existsById(issueTypeId)) {
                    log.debug("중복된 IssueType: {}", issueTypeId);
                    continue;
                }

                IssueType issueType = issueTypeMapper.toEntity(response);
                issueTypeRepository.save(issueType);
                savedTypes.add(issueType);

            } catch (Exception e) {
                log.error("[JIRA] IssueType 저장 실패 | ID: {}, Error: {}",
                        response.id(), e.getMessage());
            }
        }

        return savedTypes;
    }
    // =================================================================================================================
    @Transactional
    public void syncAllUsers() {
        log.info("[JIRA][USER SYNC 시작]");

        int startAt = 0;
        int maxResults = 100;
        int totalSaved = 0;
        int totalSkipped = 0;
        boolean hasMore = true;
        int pageCount = 0;

        while (hasMore) {
            try {
                pageCount++;

                List<JiraUserResponse> responses = jiraApiService
                        .fetchUsers(startAt, maxResults)
                        .block();

                if (responses == null || responses.isEmpty()) {
                    log.info("[JIRA][USER SYNC] 더 이상 가져올 사용자가 없습니다.");
                    break;
                }

                // DB 저장
                List<JiraUser> savedUsers = saveUsersToDB(responses);
                totalSaved += savedUsers.size();
                totalSkipped += (responses.size() - savedUsers.size());

                log.info("[JIRA][USER SYNC 진행중] Page: {}, Fetched: {}, Saved: {}, Skipped: {}",
                        pageCount, responses.size(), savedUsers.size(),
                        responses.size() - savedUsers.size());

                // 페이지네이션
                if (responses.size() < maxResults) {
                    hasMore = false;
                } else {
                    startAt += maxResults;
                }

            } catch (Exception e) {
                log.error("[JIRA][USER SYNC 실패] Page: {}, Error: {}", pageCount, e.getMessage(), e);
                throw new RuntimeException("User sync failed at page: " + pageCount, e);
            }
        }

        log.info("[JIRA][USER SYNC 완료] Total Saved: {}, Total Skipped: {}", totalSaved, totalSkipped);
    }
    private List<JiraUser> saveUsersToDB(List<JiraUserResponse> userResponses) {
        List<JiraUser> savedUsers = new ArrayList<>();

        for (JiraUserResponse response : userResponses) {
            try {
                // 중복 체크
                if (jiraUserRepository.existsById(response.accountId())) {
                    log.debug("중복된 사용자: {}", response.accountId());
                    continue;
                }

                JiraUser user = jiraUserMapper.toEntity(response);
                jiraUserRepository.save(user);
                savedUsers.add(user);

            } catch (Exception e) {
                log.error("[JIRA] 사용자 저장 실패 | AccountId: {}, Error: {}",
                        response.accountId(), e.getMessage());
            }
        }

        return savedUsers;
    }
    //==================================================================================================================
    @Transactional
    public void syncAllProjects() {
        log.info("[JIRA][PROJECT SYNC 시작]");

        int startAt = 0;
        int maxResults = 50;
        int totalSaved = 0;
        int totalSkipped = 0;
        boolean hasMore = true;
        int pageCount = 0;

        while (hasMore) {
            try {
                pageCount++;

                JiraProjectResponse response = jiraApiService
                        .fetchProjects(startAt, maxResults)
                        .block();

                if (response == null || response.values() == null || response.values().isEmpty()) {
                    log.info("[JIRA][PROJECT SYNC] 더 이상 가져올 프로젝트가 없습니다.");
                    break;
                }

                // DB 저장
                List<JiraProject> savedProjects = saveProjectsToDB(response.values());
                totalSaved += savedProjects.size();
                totalSkipped += (response.values().size() - savedProjects.size());

                log.info("[JIRA][PROJECT SYNC 진행중] Page: {}, Fetched: {}, Saved: {}, Skipped: {}",
                        pageCount, response.values().size(), savedProjects.size(),
                        response.values().size() - savedProjects.size());

                // 페이지네이션
                hasMore = !Boolean.TRUE.equals(response.isLast());
                if (hasMore) {
                    startAt += maxResults;
                }

            } catch (Exception e) {
                log.error("[JIRA][PROJECT SYNC 실패] Page: {}, Error: {}", pageCount, e.getMessage(), e);
                throw new RuntimeException("Project sync failed at page: " + pageCount, e);
            }
        }

        log.info("[JIRA][PROJECT SYNC 완료] Total Saved: {}, Total Skipped: {}", totalSaved, totalSkipped);
    }

    /**
     * 프로젝트 목록을 DB에 저장
     */
    private List<JiraProject> saveProjectsToDB(List<JiraProjectResponse.ProjectValue> projectValues) {
        List<JiraProject> savedProjects = new ArrayList<>();

        for (JiraProjectResponse.ProjectValue value : projectValues) {
            try {
                // 중복 체크
                Integer projectId = Integer.parseInt(value.id());
                if (jiraProjectRepository.existsById(projectId)) {
                    log.debug("중복된 프로젝트: {}", value.key());
                    continue;
                }

                JiraProject project = jiraProjectMapper.toEntity(value);
                jiraProjectRepository.save(project);
                savedProjects.add(project);

                log.debug("프로젝트 저장 완료: {} - {}", value.key(), value.name());

            } catch (Exception e) {
                log.error("[JIRA] 프로젝트 저장 실패 | Key: {}, Error: {}",
                        value.key(), e.getMessage());
            }
        }

        return savedProjects;
    }
}