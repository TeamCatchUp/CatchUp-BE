package com.team.catchup.jira.service;

import com.team.catchup.jira.dto.SyncStep;
import com.team.catchup.jira.dto.response.*;
import com.team.catchup.jira.entity.*;
import com.team.catchup.jira.mapper.*;
import com.team.catchup.jira.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraSyncService {

    private final JiraTransactionalSyncService transactionalSyncService;
    private final JiraProjectRepository jiraProjectRepository;

    public FullSyncResult fullSync() {
        return fullSyncFrom(SyncStep.PROJECTS, null);
    }

    public FullSyncResult fullSyncFrom(SyncStep startFrom, List<String> targetProjectKeys) {
        log.info("[JIRA][FULL SYNC] startFrom: {} | targetProjects: {}",
                startFrom, targetProjectKeys != null ? targetProjectKeys : "All");

        FullSyncResult.FullSyncResultBuilder resultBuilder = FullSyncResult.builder();

        SyncStep currentStep = startFrom;
        SyncStep lastCompletedStep = getPreviousStep(startFrom);

        try {
            // Jira Projects
            if (shouldExecute(startFrom, SyncStep.PROJECTS)) {
                currentStep = SyncStep.PROJECTS;
                SyncCount count = transactionalSyncService.syncProjects();
                resultBuilder.projects(count);
                lastCompletedStep = SyncStep.PROJECTS;
            }

            // Jira Users
            if (shouldExecute(startFrom, SyncStep.USERS)) {
                currentStep = SyncStep.USERS;
                SyncCount count = transactionalSyncService.syncUsers();
                resultBuilder.users(count);
                lastCompletedStep = SyncStep.USERS;
            }

            // Issue Types
            if (shouldExecute(startFrom, SyncStep.ISSUE_TYPES)) {
                currentStep = SyncStep.ISSUE_TYPES;
                SyncCount count = transactionalSyncService.syncIssueTypes();
                resultBuilder.issuesTypes(count);
                lastCompletedStep = SyncStep.ISSUE_TYPES;
            }

            // Project Issues
            if (shouldExecute(startFrom, SyncStep.PROJECT_ISSUES)) {
                currentStep = SyncStep.PROJECT_ISSUES;

                List<String> projectKeys = resolveTargetProjectKeys(targetProjectKeys);
                Map<String, ProjectSyncResult> projectResults = syncAllProjectIssues(projectKeys);

                resultBuilder.projectSyncResults(projectResults);

                List<String> failedKeys = projectResults.entrySet().stream()
                        .filter(e -> !e.getValue().isSuccess())
                        .map(Map.Entry::getKey)
                        .toList();

                resultBuilder.failedProjectKeys(failedKeys);
                lastCompletedStep = SyncStep.PROJECT_ISSUES;
            }

            resultBuilder.lastCompletedStep(SyncStep.COMPLETED);
            log.info("[JIRA][FULL SYNC] SUCCESS");

        } catch (Exception e) {
            log.error("[JIRA][FULL SYNC] FAILED - Failed Step: {}", currentStep, e);
            resultBuilder
                    .lastCompletedStep(lastCompletedStep)
                    .failedStep(currentStep)
                    .errorMessage(e.getMessage());
        }

        return resultBuilder.build();
    }

    public FullSyncResult retryFailedProjects(List<String> failedProjectKeys) {
        log.info("[JIRA][RETRY FAILED PROJECTS] projectKeys: {}", failedProjectKeys);

        Map<String, ProjectSyncResult> projectResults = syncAllProjectIssues(failedProjectKeys);

        List<String> failedKeys = projectResults.entrySet().stream()
                .filter(e -> !e.getValue().isSuccess())
                .map(Map.Entry::getKey)
                .toList();

        return FullSyncResult.builder()
                .lastCompletedStep(SyncStep.COMPLETED)
                .projectSyncResults(projectResults)
                .failedProjectKeys(failedKeys)
                .build();
    }

    private Map<String, ProjectSyncResult> syncAllProjectIssues(List<String> projectKeys) {
        Map<String, ProjectSyncResult> results = new HashMap<>();

        for (String projectKey : projectKeys) {
            ProjectSyncResult result = transactionalSyncService.syncSingleProjectIssue(projectKey);
            results.put(projectKey, result);
        }

        return results;
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

    private SyncStep getPreviousStep(SyncStep step) {
        int ordinal = step.ordinal();
        if (ordinal == 0) return null;
        return SyncStep.values()[ordinal - 1];
    }
}