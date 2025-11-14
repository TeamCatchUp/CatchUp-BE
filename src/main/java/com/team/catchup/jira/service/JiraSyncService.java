package com.team.catchup.jira.service;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import com.team.catchup.jira.entity.IssueMetadata;
import com.team.catchup.jira.mapper.IssueMetaDataMapper;
import com.team.catchup.jira.repository.IssueMetaDataRepository;
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
}