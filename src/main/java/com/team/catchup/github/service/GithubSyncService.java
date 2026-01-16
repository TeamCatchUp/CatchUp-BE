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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    public void fullSync(Long userId, String owner, String repo, String branch) {
        fullSyncFrom(userId, owner, repo, branch, GithubSyncStep.REPOSITORY_INFO);
    }

    /**
     * 특정 단계부터 Full Sync 재시도
     */
    public void fullSyncFrom(Long userId, String owner, String repo, String branch, GithubSyncStep startFrom) {
        log.info("[GITHUB][FULL SYNC] Starting background sync | owner: {}, repo: {}, branch: {}, startFrom: {}",
                owner, repo, branch, startFrom);

        fullSyncInternal(userId, owner, repo, branch, startFrom)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        result -> log.debug("[GITHUB][FULL SYNC] Completed successfully"),
                        error -> log.error("[GITHUB][FULL SYNC] Failed with error", error)
                );
    }

    /**
     * Full Sync 내부 구현
     */
    private Mono<Void> fullSyncInternal(Long userId, String owner, String repo, String branch, GithubSyncStep startFrom) {
        String repositoryName = owner + "/" + repo;
        long startTime = System.currentTimeMillis();

        return Mono.defer(() -> {
            publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                    "Starting Github Full Sync for " + repositoryName + " on branch: " + branch);

            // Step 1: Repository 메타데이터 동기화
            Mono<GithubRepository> repositoryMono = shouldExecute(startFrom, GithubSyncStep.REPOSITORY_INFO)
                    ? syncRepositoryInfo(userId, owner, repo, branch, repositoryName)
                    : persistenceService.findRepository(owner, repo)
                    .switchIfEmpty(Mono.error(
                            new IllegalStateException("Repository not found: " + repositoryName)
                    ));

            return repositoryMono
                    .flatMap(repository -> {
                        // syncedBranch 업데이트
                        repository.updateSyncInfo(branch, GithubRepository.SyncStatus.IN_PROGRESS);
                        return persistenceService.saveRepository(repository);
                    })
                    .flatMap(repository ->
                            // Step 2-7: 모든 동기화 단계 순차 실행
                            syncAllSteps(userId, owner, repo, branch, repositoryName, repository, startFrom)
                                    .thenReturn(repository)
                    )
                    .flatMap(repository -> {
                        // 동기화 완료 처리
                        repository.updateSyncInfo(branch, GithubRepository.SyncStatus.COMPLETED);
                        return persistenceService.saveRepository(repository);
                    })
                    .doOnSuccess(repository -> {
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
                    })
                    .doOnError(e -> {
                        log.error("[GITHUB][FULL SYNC] FAILED for {}/{}@{}", owner, repo, branch, e);
                        publishSimpleMessage(userId, SseEventType.FAILED,
                                "Github Full Sync Failed for " + repositoryName + "@" + branch + ": " + e.getMessage());
                    })
                    .then();
        });
    }

    /**
     * 모든 동기화 단계 순차 실행
     */
    private Mono<Void> syncAllSteps(Long userId, String owner, String repo, String branch,
                                    String repositoryName, GithubRepository repository, GithubSyncStep startFrom) {
        return Mono.empty()
                // Step 2: Commits
                .then(shouldExecute(startFrom, GithubSyncStep.COMMITS)
                        ? syncCommitsStep(userId, owner, repo, branch, repositoryName, repository)
                        : Mono.empty())
                // Step 3: Pull Requests
                .then(shouldExecute(startFrom, GithubSyncStep.PULL_REQUESTS)
                        ? syncPullRequestsStep(userId, owner, repo, branch, repositoryName, repository)
                        : Mono.empty())
                // Step 4: Issues
                .then(shouldExecute(startFrom, GithubSyncStep.ISSUES)
                        ? syncIssuesStep(userId, owner, repo, repositoryName, repository)
                        : Mono.empty())
                // Step 5: Comments
                .then(shouldExecute(startFrom, GithubSyncStep.COMMENTS)
                        ? syncCommentsStep(userId, owner, repo, repositoryName, repository)
                        : Mono.empty())
                // Step 6: Reviews
                .then(shouldExecute(startFrom, GithubSyncStep.REVIEWS)
                        ? syncReviewsStep(userId, owner, repo, repositoryName, repository)
                        : Mono.empty())
                // Step 7: File Changes
                .then(shouldExecute(startFrom, GithubSyncStep.FILE_CHANGES)
                        ? syncFileChangesStep(userId, owner, repo, repositoryName, repository)
                        : Mono.empty());
    }

    // ==================== Private Step Methods ====================

    private Mono<GithubRepository> syncRepositoryInfo(Long userId, String owner, String repo, String branch, String repositoryName) {
        log.info("[GITHUB][SYNC] Step 1: Syncing repository metadata");
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing repository metadata for " + repositoryName);

        return githubProcessor.processRepository(owner, repo, branch)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("Failed to sync repository metadata for " + repositoryName)
                ))
                .doOnSuccess(repository -> {
                    GithubSyncProgress progress = GithubSyncProgress.of(
                            GithubSyncStep.REPOSITORY_INFO,
                            SyncCount.of(1, 1),
                            repositoryName,
                            "Repository metadata synced for branch: " + branch
                    );
                    publishProgressMessage(userId, SseEventType.IN_PROGRESS,
                            "Repository metadata synced", progress);
                    log.info("[GITHUB][SYNC] Repository metadata completed for branch: {}", branch);
                });
    }

    private Mono<Void> syncCommitsStep(Long userId, String owner, String repo, String branch,
                                       String repositoryName, GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 2: Syncing commits for branch: {}", branch);
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing commits for " + repositoryName + "@" + branch);

        return githubProcessor.processCommits(repository, owner, repo, branch, null)
                .doOnSuccess(count -> {
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
                })
                .then();
    }

    private Mono<Void> syncPullRequestsStep(Long userId, String owner, String repo, String branch,
                                            String repositoryName, GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 3: Syncing pull requests with base: {}", branch);
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing pull requests for " + repositoryName + " (base: " + branch + ")");

        return githubProcessor.processPullRequests(repository, owner, repo, branch, "all", null)
                .doOnSuccess(count -> {
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
                })
                .then();
    }

    private Mono<Void> syncIssuesStep(Long userId, String owner, String repo,
                                      String repositoryName, GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 4: Syncing issues");
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing issues for " + repositoryName);

        return githubProcessor.processIssues(repository, owner, repo, "all", null)
                .doOnSuccess(count -> {
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
                })
                .then();
    }

    private Mono<Void> syncCommentsStep(Long userId, String owner, String repo, String repositoryName,
                                        GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 5: Syncing comments");
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing comments for " + repositoryName);

        return syncAllComments(owner, repo, repository)
                .doOnSuccess(count -> {
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
                })
                .then();
    }

    private Mono<Void> syncReviewsStep(Long userId, String owner, String repo, String repositoryName,
                                       GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 6: Syncing reviews");
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing reviews for " + repositoryName);

        return syncAllReviews(owner, repo, repository)
                .doOnSuccess(count -> {
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
                })
                .then();
    }

    private Mono<Void> syncFileChangesStep(Long userId, String owner, String repo, String repositoryName,
                                           GithubRepository repository) {
        log.info("[GITHUB][SYNC] Step 7: Syncing PR file changes");
        publishSimpleMessage(userId, SseEventType.IN_PROGRESS,
                "Syncing file changes for " + repositoryName);

        return syncAllFileChanges(owner, repo, repository)
                .doOnSuccess(count -> {
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
                })
                .then();
    }

    // ==================== Private Helper Methods ====================

    private Mono<SyncCount> syncAllComments(String owner, String repo, GithubRepository repository) {
        Mono<SyncCount> prComments = persistenceService.findUnindexedPullRequests(repository.getRepositoryId())
                .flatMapMany(Flux::fromIterable)
                .flatMap(pr ->
                        githubProcessor.processPullRequestComments(owner, repo, pr.getNumber())
                                .flatMap(count -> {
                                    // 처리 성공 시 indexedAt 업데이트
                                    if (count.saved() > 0 || count.totalFetched() == 0) {
                                        return persistenceService.markPullRequestAsIndexed(pr.getPullRequestId())
                                                .thenReturn(count);
                                    }
                                    return Mono.just(count);
                                })
                                .onErrorResume(e -> {
                                    log.error("[GITHUB][SYNC] Failed to sync comments for PR #{}", pr.getNumber(), e);
                                    return Mono.just(SyncCount.empty());
                                })
                )
                .reduce(SyncCount.empty(), (acc, count) ->
                        SyncCount.of(acc.totalFetched() + count.totalFetched(),
                                acc.saved() + count.saved())
                );

        Mono<SyncCount> issueComments = persistenceService.findUnindexedIssues(repository.getRepositoryId())
                .flatMapMany(Flux::fromIterable)
                .flatMap(issue ->
                        githubProcessor.processIssueComments(owner, repo, issue.getNumber())
                                .flatMap(count -> {
                                    // 처리 성공 시 indexedAt 업데이트
                                    if (count.saved() > 0 || count.totalFetched() == 0) {
                                        return persistenceService.markIssueAsIndexed(issue.getIssueId())
                                                .thenReturn(count);
                                    }
                                    return Mono.just(count);
                                })
                                .onErrorResume(e -> {
                                    log.error("[GITHUB][SYNC] Failed to sync comments for issue #{}", issue.getNumber(), e);
                                    return Mono.just(SyncCount.empty());
                                })
                )
                .reduce(SyncCount.empty(), (acc, count) ->
                        SyncCount.of(acc.totalFetched() + count.totalFetched(),
                                acc.saved() + count.saved())
                );

        return Mono.zip(prComments, issueComments)
                .map(tuple -> SyncCount.of(
                        tuple.getT1().totalFetched() + tuple.getT2().totalFetched(),
                        tuple.getT1().saved() + tuple.getT2().saved()
                ));
    }

    private Mono<SyncCount> syncAllReviews(String owner, String repo, GithubRepository repository) {
        return persistenceService.findUnindexedPullRequests(repository.getRepositoryId())
                .flatMapMany(Flux::fromIterable)
                .flatMap(pr ->
                        githubProcessor.processPullRequestReviews(owner, repo, pr.getNumber())
                                .flatMap(count -> {
                                    // 처리 성공 시 indexedAt 업데이트
                                    if (count.saved() > 0 || count.totalFetched() == 0) {
                                        return persistenceService.markPullRequestAsIndexed(pr.getPullRequestId())
                                                .thenReturn(count);
                                    }
                                    return Mono.just(count);
                                })
                                .onErrorResume(e -> {
                                    log.error("[GITHUB][SYNC] Failed to sync reviews for PR #{}", pr.getNumber(), e);
                                    return Mono.just(SyncCount.empty());
                                })
                )
                .reduce(SyncCount.empty(), (acc, count) ->
                        SyncCount.of(acc.totalFetched() + count.totalFetched(),
                                acc.saved() + count.saved())
                );
    }

    private Mono<SyncCount> syncAllFileChanges(String owner, String repo, GithubRepository repository) {
        return persistenceService.findUnindexedPullRequests(repository.getRepositoryId())
                .flatMapMany(Flux::fromIterable)
                .flatMap(pr ->
                        githubProcessor.processPullRequestFileChanges(owner, repo, pr.getNumber())
                                .flatMap(count -> {
                                    // 처리 성공 시 indexedAt 업데이트
                                    if (count.saved() > 0 || count.totalFetched() == 0) {
                                        return persistenceService.markPullRequestAsIndexed(pr.getPullRequestId())
                                                .thenReturn(count);
                                    }
                                    return Mono.just(count);
                                })
                                .onErrorResume(e -> {
                                    log.error("[GITHUB][SYNC] Failed to sync file changes for PR #{}", pr.getNumber(), e);
                                    return Mono.just(SyncCount.empty());
                                })
                )
                .reduce(SyncCount.empty(), (acc, count) ->
                        SyncCount.of(acc.totalFetched() + count.totalFetched(),
                                acc.saved() + count.saved())
                );
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
