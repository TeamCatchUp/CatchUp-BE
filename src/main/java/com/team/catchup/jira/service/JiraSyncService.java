package com.team.catchup.jira.service;

import com.team.catchup.jira.dto.response.JiraSyncProgress;
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
            publishSimpleMessage(userId, SseEventType.IN_PROGRESS, "Starting Jira Full Sync");

            // Jira Projects
            if (shouldExecute(startFrom, JiraSyncStep.PROJECTS)) {
                syncProjects(userId);
            }

            // Jira Users
            if (shouldExecute(startFrom, JiraSyncStep.USERS)) {
                syncUsers(userId);
            }

            // Issue Types
            if (shouldExecute(startFrom, JiraSyncStep.ISSUE_TYPES)) {
                syncIssueTypes(userId);
            }

            // Project Issues
            if (shouldExecute(startFrom, JiraSyncStep.PROJECT_ISSUES)) {
                syncProjectIssues(userId, targetProjectKeys);
            }

            long duration = System.currentTimeMillis() - startTime;
            String completeMsg = String.format("Jira Full Sync Completed | Time Used : %ds",duration/1000);

            JiraSyncProgress progress = JiraSyncProgress.of(
                    JiraSyncStep.COMPLETED,
                    null,
                    completeMsg
            );
            publishProgressMessage(userId, SseEventType.COMPLETED, completeMsg, progress);
            log.info("[JIRA][FULL SYNC] All Steps Completed - Time Used: {}ms", duration);

        } catch (Exception e) {
            log.error("[JIRA][FULL SYNC] FAILED", e);
            publishSimpleMessage(userId, SseEventType.FAILED, "Jira Full Sync Failed: " + e.getMessage());
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

    //==================================================================================================================
    private void syncProjects(String userId) {
        SyncCount count = jiraProcessor.syncProjects();

        JiraSyncProgress progress = JiraSyncProgress.of(
                JiraSyncStep.PROJECTS,
                count,
                "Project Sync Completed"
        );

        publishProgressMessage(userId, SseEventType.IN_PROGRESS, "Project Sync Completed", progress);
        log.info("[JIRA SYNC] Projects completed - total: {}, saved: {}",
                count.getTotal(), count.getSaved());
    }

    private void syncUsers(String userId) {
        SyncCount count = jiraProcessor.syncUsers();

        JiraSyncProgress progress = JiraSyncProgress.of(
                JiraSyncStep.USERS,
                count,
                "User Sync Completed"
        );

        publishProgressMessage(userId, SseEventType.IN_PROGRESS, "User Sync Completed", progress);
        log.info("[JIRA SYNC] Users completed - total: {}, saved: {}",
                count.getTotal(), count.getSaved());
    }

    private void syncIssueTypes(String userId) {
        SyncCount count = jiraProcessor.syncIssueTypes();

        JiraSyncProgress progress = JiraSyncProgress.of(
                JiraSyncStep.ISSUE_TYPES,
                count,
                "Issue Type Sync Completed"
        );

        publishProgressMessage(userId, SseEventType.IN_PROGRESS, "Issue Type Sync Completed", progress);
        log.info("[JIRA SYNC] IssueTypes completed - total: {}, saved: {}",
                count.getTotal(), count.getSaved());
    }

    private void syncProjectIssues(String userId, List<String> targetProjectKeys) {
        List<String> projectKeys = resolveTargetProjectKeys(targetProjectKeys);
        int totalProjects = projectKeys.size();
        int currentIdx = 0;

        publishSimpleMessage(userId, SseEventType.IN_PROGRESS, "Starting Jira Issue Sync");
        log.info("[JIRA SYNC] Issue sync started - projects: {}", totalProjects);

        for (String projectKey : projectKeys) {
            currentIdx++;

            ProjectSyncResult result = jiraProcessor.syncSingleProjectIssue(projectKey);

            String msg = String.format("[%d/%d] Sync completed for project: %s",
                    currentIdx, totalProjects, projectKey);

            JiraSyncProgress progress = JiraSyncProgress.ofProjectIssue(
                    JiraSyncStep.PROJECT_ISSUES,
                    result,
                    projectKey,
                    msg
            );

            publishProgressMessage(userId, SseEventType.IN_PROGRESS, msg, progress);
        }
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

    private void publishSimpleMessage(String userId, SseEventType type, String message) {
        publisher.publishEvent(new SyncEvent(userId, SyncTarget.JIRA, type, message));
    }

    private void publishProgressMessage(
            String userId,
            SseEventType type,
            String message,
            JiraSyncProgress progress
    ) {
        publisher.publishEvent(
                new SyncEvent(userId, SyncTarget.JIRA, type, message, progress)
        );
    }
}