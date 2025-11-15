package com.team.catchup.jira.service;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import com.team.catchup.jira.dto.response.IssueTypeResponse;
import com.team.catchup.jira.entity.IssueMetadata;
import com.team.catchup.jira.entity.IssueType;
import com.team.catchup.jira.mapper.IssueMetaDataMapper;
import com.team.catchup.jira.mapper.IssueTypeMapper;
import com.team.catchup.jira.repository.IssueMetaDataRepository;
import com.team.catchup.jira.repository.IssueTypeRepository;
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

    /**
     * 초기 전체 동기화 (Full Sync)
     */
    @Transactional
    public void fullSync(String projectKey, Integer maxResults) {
        log.info("[JIRA][FULL SYNC 시작] Project: {}, MaxResults: {}", projectKey, maxResults);

        String nextPageToken = null;
        int totalSaved = 0;
        int totalSkipped = 0;
        boolean hasMore = true;
        int pageCount = 0;

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

                // DB 저장
                List<IssueMetadata> savedIssues = saveIssuesToDB(response.issues());
                totalSaved += savedIssues.size();
                totalSkipped += (response.issues().size() - savedIssues.size());

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

        log.info("[JIRA][FULL SYNC 완료] Total Saved: {}, Total Skipped: {}", totalSaved, totalSkipped);
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

    /**
     * 프로젝트의 전체 이슈 개수 조회
     */
    public Integer getTotalIssueCount(String projectKey) {

        IssueMetaDataResponse response = jiraApiService
                .fetchIssuesWithToken(projectKey, null, 1, false)
                .block();

        if (response == null || response.issues() == null) {
            log.warn("Response is null for project: {}", projectKey);
            return 0;
        }

        return response.issues().size();
    }

    // 테스트를 위해 별도로 구현했지만, FullSync에 포함될 예정입니다.
    @Transactional
    public void syncAllIssueTypes() {
        log.info("[JIRA][ISSUE TYPE SYNC 시작]");

        List<IssueTypeResponse> responses = jiraApiService
                .fetchAllIssueTypes()
                .block();

        if (responses == null || responses.isEmpty()) {
            log.warn("[JIRA][ISSUE TYPE SYNC] 조회된 IssueType이 없습니다.");
            return;
        }

        int savedCount = 0;
        int updatedCount = 0;

        for (IssueTypeResponse response : responses) {
            try {
                IssueType issueType = issueTypeMapper.toEntity(response);

                // 기존 존재 여부 확인
                boolean exists = issueTypeRepository.existsById(issueType.getId());

                if (exists) {
                    updatedCount++;
                } else {
                    savedCount++;
                }

                issueTypeRepository.save(issueType);

            } catch (Exception e) {
                log.error("IssueType 저장 실패: {}", response.id(), e);
            }
        }

        log.info("[ISSUE TYPE SYNC 완료] 신규: {}, 업데이트: {}, 총: {}",
                savedCount, updatedCount, responses.size());
    }
}