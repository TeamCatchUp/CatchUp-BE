package com.team.catchup.jira.service;

import com.team.catchup.jira.dto.SyncStep;
import com.team.catchup.jira.dto.response.SyncCount;
import com.team.catchup.jira.entity.JiraProject;
import com.team.catchup.jira.repository.JiraProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraSyncService {

    private final JiraProcessor transactionalSyncService;
    private final JiraProjectRepository jiraProjectRepository;

    @Async
    public void fullSync() {
        fullSyncFrom(SyncStep.PROJECTS, null);
    }

    @Async
    public void fullSyncFrom(SyncStep startFrom, List<String> targetProjectKeys) {
        log.info("[JIRA][FULL SYNC] Background Process Started | startFrom: {}", startFrom);
        long startTime = System.currentTimeMillis();

        try {
            // Jira Projects
            if (shouldExecute(startFrom, SyncStep.PROJECTS)) {
                SyncCount count = transactionalSyncService.syncProjects();
                log.info("[JIRA][FULL SYNC] Projects Completed | Total: {}, Saved: {}", count.getTotal(), count.getSaved());
            }

            // Jira Users
            if (shouldExecute(startFrom, SyncStep.USERS)) {
                SyncCount count = transactionalSyncService.syncUsers();
                log.info("[JIRA][FULL SYNC] Users Completed | Total: {}, Saved: {}", count.getTotal(), count.getSaved());
            }

            // Issue Types
            if (shouldExecute(startFrom, SyncStep.ISSUE_TYPES)) {
                SyncCount count = transactionalSyncService.syncIssueTypes();
                log.info("[JIRA][FULL SYNC] Issue Types Completed | Total: {}, Saved: {}", count.getTotal(), count.getSaved());
            }

            // Project Issues
            if (shouldExecute(startFrom, SyncStep.PROJECT_ISSUES)) {
                List<String> projectKeys = resolveTargetProjectKeys(targetProjectKeys);
                log.info("[JIRA][FULL SYNC] Issue Sync Started for {} projects", projectKeys.size());

                for (String projectKey : projectKeys) {
                    transactionalSyncService.syncSingleProjectIssue(projectKey);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("[JIRA][FULL SYNC] All Steps Completed - Time Used: {}ms", duration);

        } catch (Exception e) {
            log.error("[JIRA][FULL SYNC] FAILED", e);
        }
    }

    @Async
    public void retryFailedProjects(List<String> failedProjectKeys) {
        log.info("[JIRA][RETRY] Retrying failed projects: {}", failedProjectKeys);

        for (String projectKey : failedProjectKeys) {
            try {
                transactionalSyncService.syncSingleProjectIssue(projectKey);
            } catch (Exception e) {
                log.error("[JIRA][RETRY] Project {} Failed again", projectKey, e);
            }
        }
        log.info("[JIRA][RETRY] Completed");
    }

    private List<String> resolveTargetProjectKeys(List<String> targetProjectKeys) {
        if (targetProjectKeys != null && !targetProjectKeys.isEmpty()) {
            return targetProjectKeys;
        }
        return jiraProjectRepository.findAll().stream()
                .map(JiraProject::getProjectKey)
                .toList();
    }

    private boolean shouldExecute(SyncStep startFrom, SyncStep target) {
        return startFrom.ordinal() <= target.ordinal();
    }
}