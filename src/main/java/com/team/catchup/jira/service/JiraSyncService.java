package com.team.catchup.jira.service;

import com.team.catchup.common.sse.dto.JiraSyncProgress;
import com.team.catchup.common.sse.dto.SseEventType;
import com.team.catchup.common.sse.dto.SyncTarget;
import com.team.catchup.common.sse.event.SyncEvent;
import com.team.catchup.jira.dto.JiraSyncStep;
import com.team.catchup.common.sse.dto.SyncCount;
import com.team.catchup.jira.dto.response.ProjectSyncResult;
import com.team.catchup.jira.entity.JiraProject;
import com.team.catchup.jira.repository.JiraProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraSyncService {

    private final JiraProcessor jiraProcessor;
    private final JiraProjectRepository jiraProjectRepository;
    private final ApplicationEventPublisher publisher;

    @Async
    public void fullSync(String userId) {
        fullSyncFrom(userId, JiraSyncStep.PROJECTS, null);
    }

    @Async
    public void fullSyncFrom(String userId,JiraSyncStep startFrom, List<String> targetProjectKeys) {
        log.info("[JIRA][FULL SYNC] Background Process Started | startFrom: {}", startFrom);
        long startTime = System.currentTimeMillis();

        try {
            publisher.publishEvent(new SyncEvent(userId, SyncTarget.JIRA, SseEventType.IN_PROGRESS, "Starting Jira Full Sync"));

            // Jira Projects
            if (shouldExecute(startFrom, JiraSyncStep.PROJECTS)) {
                SyncCount count = jiraProcessor.syncProjects();

                JiraSyncProgress progress = JiraSyncProgress.of(
                        JiraSyncStep.PROJECTS, count, "Project Sync Completed");
                publisher.publishEvent(new SyncEvent(userId, SyncTarget.JIRA, SseEventType.IN_PROGRESS, progress));

                log.info("[JIRA][FULL SYNC] Projects Completed | Total: {}, Saved: {}", count.getTotal(), count.getSaved());
            }

            // Jira Users
            if (shouldExecute(startFrom, JiraSyncStep.USERS)) {
                SyncCount count = jiraProcessor.syncUsers();

                JiraSyncProgress progress = JiraSyncProgress.of(
                        JiraSyncStep.USERS, count, "User Sync Completed");
                publisher.publishEvent(new SyncEvent(userId, SyncTarget.JIRA, SseEventType.IN_PROGRESS, progress));

                log.info("[JIRA][FULL SYNC] Users Completed | Total: {}, Saved: {}", count.getTotal(), count.getSaved());
            }

            // Issue Types
            if (shouldExecute(startFrom, JiraSyncStep.ISSUE_TYPES)) {
                SyncCount count = jiraProcessor.syncIssueTypes();

                JiraSyncProgress progress = JiraSyncProgress.of(
                        JiraSyncStep.ISSUE_TYPES, count, "Issue Type Sync Completed");
                publisher.publishEvent(new SyncEvent(userId, SyncTarget.JIRA, SseEventType.IN_PROGRESS, progress));

                log.info("[JIRA][FULL SYNC] Issue Types Completed | Total: {}, Saved: {}", count.getTotal(), count.getSaved());
            }

            // Project Issues
            if (shouldExecute(startFrom, JiraSyncStep.PROJECT_ISSUES)) {
                List<String> projectKeys = resolveTargetProjectKeys(targetProjectKeys);
                int totalProjects = projectKeys.size();
                int currentIdx = 0;

                publisher.publishEvent(new SyncEvent(userId, SyncTarget.JIRA, SseEventType.IN_PROGRESS,
                        "Starting Jira Issue Sync"));
                log.info("[JIRA][FULL SYNC] Issue Sync Started for {} projects", projectKeys.size());

                for (String projectKey : projectKeys) {
                    currentIdx++;

                    ProjectSyncResult result = jiraProcessor.syncSingleProjectIssue(projectKey);

                    String msg = String.format("[%d/%d] Sync Completed for Project Key : (%s)",
                            currentIdx, totalProjects, projectKey);

                    JiraSyncProgress progress = JiraSyncProgress.ofProjectIssue(JiraSyncStep.PROJECT_ISSUES, result , projectKey, msg);
                    publisher.publishEvent(new SyncEvent(userId, SyncTarget.JIRA, SseEventType.IN_PROGRESS, progress));
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            String completeMsg = String.format("Jira Full Sync Completed | Time Used : %ds",duration/1000);

            JiraSyncProgress progress = JiraSyncProgress.builder()
                    .step(JiraSyncStep.COMPLETED)
                    .message(completeMsg)
                    .build();

            publisher.publishEvent(new SyncEvent(userId, SyncTarget.JIRA, SseEventType.COMPLETED, progress));
            log.info("[JIRA][FULL SYNC] All Steps Completed - Time Used: {}ms", duration);

        } catch (Exception e) {
            log.error("[JIRA][FULL SYNC] FAILED", e);

            publisher.publishEvent(new SyncEvent(userId, SyncTarget.JIRA, SseEventType.FAILED, "Jira Full Sync Failed" + e.getMessage()));
        }
    }

    @Async
    public void retryFailedProjects(List<String> failedProjectKeys) {
        log.info("[JIRA][RETRY] Retrying failed projects: {}", failedProjectKeys);

        for (String projectKey : failedProjectKeys) {
            try {
                jiraProcessor.syncSingleProjectIssue(projectKey);
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

    private boolean shouldExecute(JiraSyncStep startFrom, JiraSyncStep target) {
        return startFrom.ordinal() <= target.ordinal();
    }
}