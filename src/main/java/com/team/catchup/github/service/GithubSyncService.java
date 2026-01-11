package com.team.catchup.github.service;

import com.team.catchup.common.sse.dto.SseEventType;
import com.team.catchup.common.sse.dto.SyncTarget;
import com.team.catchup.common.sse.event.SyncEvent;
import com.team.catchup.github.dto.GithubSyncStep;
import com.team.catchup.github.dto.response.GithubSyncProgress;
import com.team.catchup.github.dto.response.SyncCount;
import com.team.catchup.github.entity.GithubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubSyncService {

    private final GithubProcessor githubProcessor;
    private final GithubPersistenceService persistenceService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Full Repository Sync
     */
    @Async
    public void fullSync(Long userId, String owner, String repo, String branch) {
        fullSyncFrom(userId, owner, repo, branch, GithubSyncStep.REPOSITORY_INFO);
    }

    /**
     * 특정 단계부터 Full Sync 재시도
     */
    @Async
    public void fullSyncFrom(Long userId, String owner, String repo, String branch, GithubSyncStep startFrom) {
        log.info("[GITHUB][FULL SYNC] Background Process Started | owner: {}, repo: {}, branch: {}, startFrom: {}",
                owner, repo, branch, startFrom);
        String repositoryName = owner + "/" + repo;
        long startTime = System.currentTimeMillis();

        try {
            publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                    "Starting Github Full Sync for " + repositoryName + " on branch: " + branch);

            GithubRepository repository = null;

            // Step 1: Repository 메타데이터 동기화
            if (shouldExecute(startFrom, GithubSyncStep.REPOSITORY_INFO)) {
                repository = syncRepositoryInfo(userId, owner, repo, branch, repositoryName);
            }

            // Repository 정보가 없으면 조회
            if (repository == null) {
                repository = persistenceService.findRepository(owner, repo);
                if (repository == null) {
                    throw new IllegalStateException("Repository not found: " + repositoryName);
                }
            }

            // syncedBranch 업데이트
            repository.updateSyncInfo(branch, GithubRepository.SyncStatus.IN_PROGRESS);
            persistenceService.saveRepository(repository);

            // Step 2: Commits 동기화
            if (shouldExecute(startFrom, GithubSyncStep.COMMITS)) {
                syncCommitsStep(userId, owner, repo, branch, repositoryName, repository);
            }

            // Step 3: Pull Requests 동기화
            if (shouldExecute(startFrom, GithubSyncStep.PULL_REQUESTS)) {
                syncPullRequestsStep(userId, owner, repo, branch, repositoryName, repository);
            }

            // Step 4: Issues 동기화
            if (shouldExecute(startFrom, GithubSyncStep.ISSUES)) {
                syncIssuesStep(userId, owner, repo, repositoryName, repository);
            }

            // Step 5: Comments 동기화
            if (shouldExecute(startFrom, GithubSyncStep.COMMENTS)) {
                syncCommentsStep(userId, owner, repo, repositoryName, repository);
            }

            // Step 6: Reviews 동기화
            if (shouldExecute(startFrom, GithubSyncStep.REVIEWS)) {
                syncReviewsStep(userId, owner, repo, repositoryName, repository);
            }

            // Step 7: File Changes 동기화 (PR만)
            if (shouldExecute(startFrom, GithubSyncStep.FILE_CHANGES)) {
                syncFileChangesStep(userId, owner, repo, repositoryName, repository);
            }

            // Update repository sync status
            repository.updateSyncInfo(branch, GithubRepository.SyncStatus.COMPLETED);
            persistenceService.saveRepository(repository);

            long duration = System.currentTimeMillis() - startTime;
            String completeMsg = String.format("Github Full Sync Completed for %s@%s | Time Used: %ds",
                    repositoryName, branch, duration / 1000);

            GithubSyncProgress progress = GithubSyncProgress.of(
                    GithubSyncStep.COMPLETED,
                    SyncCount.empty(),
                    repositoryName,
                    completeMsg
            );
            publishProgressMessage(userId, SseEventType.COMPLETED, completeMsg, progress);
            log.info("[GITHUB][FULL SYNC] All Steps Completed - Time Used: {}ms", duration);

        } catch (Exception e) {
            log.error("[GITHUB][FULL SYNC] FAILED for {}/{}@{}", owner, repo, branch, e);
            publishSimpleMessage(userId, SseEventType.FAILED,
                    "Github Full Sync Failed for " + repositoryName + "@" + branch + ": " + e.getMessage());
        }
    }

    /**
     * Repository 메타데이터만 동기화
     */
    @Async
    public void syncRepositoryMetadata(String userId, String owner, String repo) {
        log.info("[GITHUB][SYNC] Syncing repository metadata for {}/{}", owner, repo);

        try {
            githubProcessor.processRepository(owner, repo);
            log.info("[GITHUB][SYNC] Repository metadata synced for {}/{}", owner, repo);
        } catch (Exception e) {
            log.error("[GITHUB][SYNC] Failed to sync repository metadata for {}/{}", owner, repo, e);
        }
    }

    /**
     * Commits만 동기화
     */
    @Async
    public void syncCommits(String owner, String repo, String branch, String since) {
        log.info("[GITHUB][SYNC] Syncing commits for {}/{} on branch: {}", owner, repo, branch);

        try {
            GithubRepository repository = persistenceService.findRepository(owner, repo);
            if (repository == null) {
                log.error("[GITHUB][SYNC] Repository not found: {}/{}", owner, repo);
                return;
            }

            SyncCount count = githubProcessor.processCommits(repository, owner, repo, branch, since);
            log.info("[GITHUB][SYNC] Commits synced: {}/{} for {}/{}@{}",
                    count.saved(), count.totalFetched(), owner, repo, branch);
        } catch (Exception e) {
            log.error("[GITHUB][SYNC] Failed to sync commits for {}/{}", owner, repo, e);
        }
    }

    /**
     * Pull Requests만 동기화
     */
    @Async
    public void syncPullRequests(String owner, String repo, String branch, String state, String since) {
        log.info("[GITHUB][SYNC] Syncing pull requests for {}/{} with base: {}", owner, repo, branch);

        try {
            GithubRepository repository = persistenceService.findRepository(owner, repo);
            if (repository == null) {
                log.error("[GITHUB][SYNC] Repository not found: {}/{}", owner, repo);
                return;
            }

            SyncCount count = githubProcessor.processPullRequests(repository, owner, repo, branch, state, since);
            log.info("[GITHUB][SYNC] Pull requests synced: {}/{} for {}/{}@{}",
                    count.saved(), count.totalFetched(), owner, repo, branch);
        } catch (Exception e) {
            log.error("[GITHUB][SYNC] Failed to sync pull requests for {}/{}", owner, repo, e);
        }
    }

    /**
     * Issues만 동기화
     */
    @Async
    public void syncIssues(String owner, String repo, String state, String since) {
        log.info("[GITHUB][SYNC] Syncing issues for {}/{}", owner, repo);

        try {
            GithubRepository repository = persistenceService.findRepository(owner, repo);
            if (repository == null) {
                log.error("[GITHUB][SYNC] Repository not found: {}/{}", owner, repo);
                return;
            }

            SyncCount count = githubProcessor.processIssues(repository, owner, repo, state, since);
            log.info("[GITHUB][SYNC] Issues synced: {}/{} for {}/{}",
                    count.saved(), count.totalFetched(), owner, repo);
        } catch (Exception e) {
            log.error("[GITHUB][SYNC] Failed to sync issues for {}/{}", owner, repo, e);
        }
    }

    // ==================== Private Step Methods ====================

    private GithubRepository syncRepositoryInfo(Long userId, String owner, String repo, String branch, String repositoryName) {
        log.info("[GITHUB][SYNC] Step 1: Syncing repository metadata");
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing repository metadata for " + repositoryName);

        GithubRepository repository = githubProcessor.processRepository(owner, repo);
        if (repository == null) {
            throw new IllegalStateException("Failed to sync repository metadata for " + repositoryName);
        }

        // syncedBranch 설정
        repository.updateSyncInfo(branch, GithubRepository.SyncStatus.IN_PROGRESS);
        persistenceService.saveRepository(repository);

        GithubSyncProgress progress = GithubSyncProgress.of(
                GithubSyncStep.REPOSITORY_INFO,
                SyncCount.of(1, 1),
                repositoryName,
                "Repository metadata synced for branch: " + branch
        );
        publishProgressMessage(userId, SseEventType.IN_PROGRESS,
                "Repository metadata synced", progress);
        log.info("[GITHUB][SYNC] Repository metadata completed for branch: {}", branch);

        return repository;
    }

    private void syncCommitsStep(Long userId, String owner, String repo, String branch,
                                 String repositoryName, GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 2: Syncing commits for branch: {}", branch);
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing commits for " + repositoryName + "@" + branch);

        SyncCount count = githubProcessor.processCommits(repository, owner, repo, branch, null);

        GithubSyncProgress progress = GithubSyncProgress.of(
                GithubSyncStep.COMMITS,
                count,
                repositoryName,
                "Commits synced: " + count.saved() + " (branch: " + branch + ")"
        );
        publishProgressMessage(userId, SseEventType.IN_PROGRESS,
                "Commits synced: " + count.saved(), progress);
        log.info("[GITHUB][SYNC] Commits completed - total: {}, saved: {}",
                count.totalFetched(), count.saved());
    }

    private void syncPullRequestsStep(Long userId, String owner, String repo, String branch,
                                      String repositoryName, GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 3: Syncing pull requests with base: {}", branch);
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing pull requests for " + repositoryName + " (base: " + branch + ")");

        SyncCount count = githubProcessor.processPullRequests(repository, owner, repo, branch, "all", null);

        GithubSyncProgress progress = GithubSyncProgress.of(
                GithubSyncStep.PULL_REQUESTS,
                count,
                repositoryName,
                "Pull requests synced: " + count.saved() + " (base: " + branch + ")"
        );
        publishProgressMessage(userId, SseEventType.IN_PROGRESS,
                "Pull requests synced: " + count.saved(), progress);
        log.info("[GITHUB][SYNC] Pull requests completed - total: {}, saved: {}",
                count.totalFetched(), count.saved());
    }

    private void syncIssuesStep(Long userId, String owner, String repo,
                                String repositoryName, GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 4: Syncing issues");
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing issues for " + repositoryName);

        SyncCount count = githubProcessor.processIssues(repository, owner, repo, "all", null);

        GithubSyncProgress progress = GithubSyncProgress.of(
                GithubSyncStep.ISSUES,
                count,
                repositoryName,
                "Issues synced: " + count.saved()
        );
        publishProgressMessage(userId, SseEventType.IN_PROGRESS,
                "Issues synced: " + count.saved(), progress);
        log.info("[GITHUB][SYNC] Issues completed - total: {}, saved: {}",
                count.totalFetched(), count.saved());
    }

    private void syncCommentsStep(Long userId, String owner, String repo, String repositoryName,
                                  GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 5: Syncing comments");
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing comments for " + repositoryName);

        SyncCount count = syncAllComments(owner, repo, repository);

        GithubSyncProgress progress = GithubSyncProgress.of(
                GithubSyncStep.COMMENTS,
                count,
                repositoryName,
                "Comments synced: " + count.saved()
        );
        publishProgressMessage(userId, SseEventType.IN_PROGRESS,
                "Comments synced: " + count.saved(), progress);
        log.info("[GITHUB][SYNC] Comments completed - total: {}, saved: {}",
                count.totalFetched(), count.saved());
    }

    private void syncReviewsStep(Long userId, String owner, String repo, String repositoryName,
                                 GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 6: Syncing reviews");
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing reviews for " + repositoryName);

        SyncCount count = syncAllReviews(owner, repo, repository);

        GithubSyncProgress progress = GithubSyncProgress.of(
                GithubSyncStep.REVIEWS,
                count,
                repositoryName,
                "Reviews synced: " + count.saved()
        );
        publishProgressMessage(userId, SseEventType.IN_PROGRESS,
                "Reviews synced: " + count.saved(), progress);
        log.info("[GITHUB][SYNC] Reviews completed - total: {}, saved: {}",
                count.totalFetched(), count.saved());
    }

    private void syncFileChangesStep(Long userId, String owner, String repo, String repositoryName,
                                     GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 7: Syncing PR file changes");
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing file changes for " + repositoryName);

        SyncCount count = syncAllFileChanges(owner, repo, repository);

        GithubSyncProgress progress = GithubSyncProgress.of(
                GithubSyncStep.FILE_CHANGES,
                count,
                repositoryName,
                "File changes synced: " + count.saved()
        );
        publishProgressMessage(userId, SseEventType.IN_PROGRESS,
                "File changes synced: " + count.saved(), progress);
        log.info("[GITHUB][SYNC] File changes completed - total: {}, saved: {}",
                count.totalFetched(), count.saved());
    }

    // ==================== Private Helper Methods ====================

    private SyncCount syncAllComments(String owner, String repo, GithubRepository repository) {
        int totalFetched = 0;
        int totalSaved = 0;

        // Sync PR comments
        for (var pr : persistenceService.findUnindexedPullRequests(repository.getRepositoryId())) {
            try {
                SyncCount count = githubProcessor.processPullRequestComments(owner, repo, pr.getNumber());
                totalFetched += count.totalFetched();
                totalSaved += count.saved();
            } catch (Exception e) {
                log.error("[GITHUB][SYNC] Failed to sync comments for PR #{}", pr.getNumber(), e);
            }
        }

        // Sync issue comments
        for (var issue : persistenceService.findUnindexedIssues(repository.getRepositoryId())) {
            try {
                SyncCount count = githubProcessor.processIssueComments(owner, repo, issue.getNumber());
                totalFetched += count.totalFetched();
                totalSaved += count.saved();
            } catch (Exception e) {
                log.error("[GITHUB][SYNC] Failed to sync comments for issue #{}", issue.getNumber(), e);
            }
        }

        return SyncCount.of(totalFetched, totalSaved);
    }

    private SyncCount syncAllReviews(String owner, String repo, GithubRepository repository) {
        int totalFetched = 0;
        int totalSaved = 0;

        for (var pr : persistenceService.findUnindexedPullRequests(repository.getRepositoryId())) {
            try {
                SyncCount count = githubProcessor.processPullRequestReviews(owner, repo, pr.getNumber());
                totalFetched += count.totalFetched();
                totalSaved += count.saved();
            } catch (Exception e) {
                log.error("[GITHUB][SYNC] Failed to sync reviews for PR #{}", pr.getNumber(), e);
            }
        }

        return SyncCount.of(totalFetched, totalSaved);
    }

    private SyncCount syncAllFileChanges(String owner, String repo, GithubRepository repository) {
        int totalFetched = 0;
        int totalSaved = 0;

        for (var pr : persistenceService.findUnindexedPullRequests(repository.getRepositoryId())) {
            try {
                SyncCount count = githubProcessor.processPullRequestFileChanges(owner, repo, pr.getNumber());
                totalFetched += count.totalFetched();
                totalSaved += count.saved();
            } catch (Exception e) {
                log.error("[GITHUB][SYNC] Failed to sync file changes for PR #{}", pr.getNumber(), e);
            }
        }

        return SyncCount.of(totalFetched, totalSaved);
    }

    private boolean shouldExecute(GithubSyncStep startFrom, GithubSyncStep target) {
        return startFrom.ordinal() <= target.ordinal();
    }

    private void publishSimpleMessage(Long userId, SseEventType type, String message) {
        eventPublisher.publishEvent(new SyncEvent(userId, SyncTarget.GITHUB, type, message));
    }

    private void publishProgressMessage(Long userId, SseEventType type, String message,
                                        GithubSyncProgress progress) {
        eventPublisher.publishEvent(
                new SyncEvent(userId, SyncTarget.GITHUB, type, message, progress)
        );
    }
}