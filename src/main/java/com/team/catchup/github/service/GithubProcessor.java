package com.team.catchup.github.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.catchup.github.dto.internal.*;
import com.team.catchup.github.dto.response.SyncCount;
import com.team.catchup.github.entity.*;
import com.team.catchup.github.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import static com.team.catchup.common.config.RabbitConfig.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubProcessor {

    private final GithubApiService githubApiService;
    private final GithubPersistenceService persistenceService;
    private final RabbitTemplate rabbitTemplate;

    private final GithubRepositoryMapper repositoryMapper;
    private final GithubCommitMapper commitMapper;
    private final GithubPullRequestMapper pullRequestMapper;
    private final GithubIssueMapper issueMapper;
    private final GithubCommentMapper commentMapper;
    private final GithubReviewMapper reviewMapper;
    private final GithubFileChangeMapper fileChangeMapper;

    /**
     * Repository 메타데이터 동기화
     */
    public Mono<GithubRepository> processRepository(String owner, String repo, String branch) {
        log.info("[GITHUB][PROCESSOR] Processing repository: {}/{} on branch: {}", owner, repo, branch);

        return githubApiService.getRepository(owner, repo)
                .map(repositoryMapper::toEntity)
                .map(repository -> {
                    repository.updateSyncInfo(branch, GithubRepository.SyncStatus.IN_PROGRESS);
                    return repository;
                })
                .flatMap(repository -> persistenceService.saveRepository(repository))
                .flatMap(saved ->
                        publishRepositoryMessage(saved)
                                .thenReturn(saved)
                )
                .doOnSuccess(saved ->
                        log.info("[GITHUB][PROCESSOR] Repository saved: {}/{} with branch: {}", owner, repo, branch)
                )
                .doOnError(e ->
                        log.error("[GITHUB][PROCESSOR] Failed to process repository: {}/{}", owner, repo, e)
                );
    }

    /**
     * Commits 동기화
     */
    public Mono<SyncCount> processCommits(GithubRepository repository, String owner, String repo, String branch, String since) {
        log.info("[GITHUB][PROCESSOR] Processing commits for {}/{} on branch: {}", owner, repo, branch);

        return githubApiService.getCommits(owner, repo, branch, since)
                .flatMap(commitNode ->
                        githubApiService.getCommit(owner, repo, commitNode.get("sha").asText())
                )
                .collectList()
                .flatMap(commitDetailNodes -> {
                    List<GithubCommit> commits = new ArrayList<>();
                    List<GithubFileChange> allFileChanges = new ArrayList<>();

                    for (JsonNode detailNode : commitDetailNodes) {
                        GithubCommit commit = commitMapper.toEntity(detailNode, repository);
                        commits.add(commit);

                        JsonNode filesNode = detailNode.get("files");
                        if (filesNode != null && filesNode.isArray()) {
                            for (JsonNode fileNode : filesNode) {
                                GithubFileChange fileChange = fileChangeMapper.toEntityFromCommit(
                                        fileNode, repository, commit.getSha()
                                );
                                allFileChanges.add(fileChange);
                            }
                        }
                    }

                    // 실행계획으로 준비함
                    Mono<Integer> saveCommitsMono = persistenceService.saveAllCommits(commits);
                    Mono<Integer> saveFilesMono = persistenceService.saveAllFileChanges(allFileChanges);

                    // 위의 두 개의 Mono를 병렬로 실행한 뒤 결과를 튜플로 결합하는 방식
                    return Mono.zip(saveCommitsMono, saveFilesMono)
                            .map(tuple -> {
                                int savedCommits = tuple.getT1();
                                int savedFiles = tuple.getT2();
                                log.info("[GITHUB][PROCESSOR] Commits saved - Commits: {}, FileChanges: {}",
                                        savedCommits, savedFiles);
                                return SyncCount.of(commits.size(), savedCommits);
                            });
                })
                .doOnError(e ->
                        log.error("[GITHUB][PROCESSOR] Failed to process commits for {}/{}", owner, repo, e)
                )
                .onErrorReturn(SyncCount.empty());
    }

    /**
     * Pull Requests 동기화
     */
    public Mono<SyncCount> processPullRequests(GithubRepository repository, String owner, String repo, String branch, String state, String since) {
        log.info("[GITHUB][PROCESSOR] Processing pull requests for {}/{} with base: {}", owner, repo, branch);

        return githubApiService.getPullRequests(owner, repo, branch, state, since)
                .map(prNode -> pullRequestMapper.toEntity(prNode, repository))
                .collectList()
                .flatMap(pullRequests ->
                        persistenceService.saveAllPullRequests(pullRequests)
                                .flatMap(saved ->
                                        publishPullRequestMessages(pullRequests)
                                                .thenReturn(SyncCount.of(pullRequests.size(), saved))
                                )
                )
                .doOnError(e ->
                        log.error("[GITHUB][PROCESSOR] Failed to process pull requests for {}/{}", owner, repo, e)
                )
                .onErrorReturn(SyncCount.empty());
    }

    /**
     * Issues 동기화 (branch 무관)
     */
    public Mono<SyncCount> processIssues(GithubRepository repository, String owner, String repo, String state, String since) {
        log.info("[GITHUB][PROCESSOR] Processing issues for {}/{}", owner, repo);

        return githubApiService.getIssues(owner, repo, state, since)
                .filter(issueNode -> !issueNode.has("pull_request"))
                .map(issueNode -> issueMapper.toEntity(issueNode, repository))
                .collectList()
                .flatMap(issues ->
                        persistenceService.saveAllIssues(issues)
                                .flatMap(saved ->
                                        publishIssueMessages(issues)
                                                .thenReturn(SyncCount.of(issues.size(), saved))
                                )
                )
                .doOnError(e ->
                        log.error("[GITHUB][PROCESSOR] Failed to process issues for {}/{}", owner, repo, e)
                )
                .onErrorReturn(SyncCount.empty());
    }

    /**
     * PR Reviews 동기화
     */
    public Mono<SyncCount> processPullRequestReviews(String owner, String repo, int prNumber) {
        log.info("[GITHUB][PROCESSOR] Processing reviews for PR #{}", prNumber);

        return persistenceService.findRepository(owner, repo)
                .flatMap(repository -> {
                    if (repository == null) {
                        log.error("[GITHUB][PROCESSOR] Repository not found: {}/{}", owner, repo);
                        return Mono.just(SyncCount.empty());
                    }

                    return persistenceService.findPullRequestByNumber(repository.getRepositoryId(), prNumber)
                            .flatMap(pullRequest -> {
                                if (pullRequest == null) {
                                    log.error("[GITHUB][PROCESSOR] Pull request not found: #{}", prNumber);
                                    return Mono.just(SyncCount.empty());
                                }

                                return githubApiService.getPullRequestReviews(owner, repo, prNumber)
                                        .map(reviewNode -> reviewMapper.toEntity(reviewNode, repository, pullRequest))
                                        .collectList()
                                        .flatMap(reviews ->
                                                persistenceService.saveAllReviews(reviews)
                                                        .map(saved -> SyncCount.of(reviews.size(), saved))
                                        );
                            });
                })
                .doOnError(e ->
                        log.error("[GITHUB][PROCESSOR] Failed to process reviews", e)
                )
                .onErrorReturn(SyncCount.empty());
    }

    /**
     * Issue Comments 동기화
     */
    public Mono<SyncCount> processIssueComments(String owner, String repo, int issueNumber) {
        log.info("[GITHUB][PROCESSOR] Processing issue comments for #{}", issueNumber);

        return persistenceService.findRepository(owner, repo)
                .flatMap(repository -> {
                    if (repository == null) {
                        log.error("[GITHUB][PROCESSOR] Repository not found: {}/{}", owner, repo);
                        return Mono.just(SyncCount.empty());
                    }

                    return persistenceService.findIssueByNumber(repository.getRepositoryId(), issueNumber)
                            .flatMap(issue -> {
                                if (issue == null) {
                                    log.error("[GITHUB][PROCESSOR] Issue not found: #{}", issueNumber);
                                    return Mono.just(SyncCount.empty());
                                }

                                return githubApiService.getIssueComments(owner, repo, issueNumber)
                                        .map(commentNode -> commentMapper.toIssueCommentEntity(commentNode, repository, issue))
                                        .collectList()
                                        .flatMap(comments ->
                                                persistenceService.saveAllComments(comments)
                                                        .map(saved -> SyncCount.of(comments.size(), saved))
                                        );
                            });
                })
                .doOnError(e ->
                        log.error("[GITHUB][PROCESSOR] Failed to process issue comments", e)
                )
                .onErrorReturn(SyncCount.empty());
    }

    /**
     * PR Review Comments 동기화
     */
    public Mono<SyncCount> processPullRequestComments(String owner, String repo, int prNumber) {
        log.info("[GITHUB][PROCESSOR] Processing PR review comments for #{}", prNumber);

        return persistenceService.findRepository(owner, repo)
                .flatMap(repository -> {
                    if (repository == null) {
                        log.error("[GITHUB][PROCESSOR] Repository not found: {}/{}", owner, repo);
                        return Mono.just(SyncCount.empty());
                    }

                    return persistenceService.findPullRequestByNumber(repository.getRepositoryId(), prNumber)
                            .flatMap(pullRequest -> {
                                if (pullRequest == null) {
                                    log.error("[GITHUB][PROCESSOR] Pull request not found: #{}", prNumber);
                                    return Mono.just(SyncCount.empty());
                                }

                                return githubApiService.getPullRequestReviewComments(owner, repo, prNumber)
                                        .map(commentNode -> commentMapper.toReviewCommentEntity(commentNode, repository, pullRequest))
                                        .collectList()
                                        .flatMap(comments ->
                                                persistenceService.saveAllComments(comments)
                                                        .map(saved -> SyncCount.of(comments.size(), saved))
                                        );
                            });
                })
                .doOnError(e ->
                        log.error("[GITHUB][PROCESSOR] Failed to process PR comments", e)
                )
                .onErrorReturn(SyncCount.empty());
    }

    /**
     * PR File Changes 동기화
     */
    public Mono<SyncCount> processPullRequestFileChanges(String owner, String repo, int prNumber) {
        log.info("[GITHUB][PROCESSOR] Processing file changes for PR #{}", prNumber);

        return persistenceService.findRepository(owner, repo)
                .flatMap(repository -> {
                    if (repository == null) {
                        log.error("[GITHUB][PROCESSOR] Repository not found: {}/{}", owner, repo);
                        return Mono.just(SyncCount.empty());
                    }

                    return persistenceService.findPullRequestByNumber(repository.getRepositoryId(), prNumber)
                            .flatMap(pullRequest -> {
                                if (pullRequest == null) {
                                    log.error("[GITHUB][PROCESSOR] Pull request not found: #{}", prNumber);
                                    return Mono.just(SyncCount.empty());
                                }

                                return githubApiService.getPullRequestFiles(owner, repo, prNumber)
                                        .map(fileNode -> fileChangeMapper.toEntityFromPullRequest(fileNode, repository, pullRequest))
                                        .collectList()
                                        .flatMap(fileChanges ->
                                                persistenceService.saveAllFileChanges(fileChanges)
                                                        .map(saved -> SyncCount.of(fileChanges.size(), saved))
                                        );
                            });
                })
                .doOnError(e ->
                        log.error("[GITHUB][PROCESSOR] Failed to process file changes", e)
                )
                .onErrorReturn(SyncCount.empty());
    }

    // ==================== RabbitMQ Publishing ====================

    private Mono<Void> publishRepositoryMessage(GithubRepository repository) {
        return Mono.fromRunnable(() -> {
                    try {
                        log.info("[GITHUB][MQ] Publishing repository to RabbitMQ: {}/{}",
                                repository.getOwner(), repository.getName());
                        GithubRepositoryRabbitRequest request = GithubRepositoryRabbitRequest.from(repository);
                        rabbitTemplate.convertAndSend(GITHUB_REPOSITORY_QUEUE, request);
                    } catch (Exception e) {
                        log.error("[GITHUB][MQ] Failed to publish repository message", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private Mono<Void> publishPullRequestMessages(List<GithubPullRequest> pullRequests) {
        return Flux.fromIterable(pullRequests)
                .flatMap(pr -> Mono.fromRunnable(() -> {
                                    try {
                                        GithubPullRequestRabbitRequest request = GithubPullRequestRabbitRequest.from(pr);
                                        rabbitTemplate.convertAndSend(GITHUB_PULL_REQUEST_QUEUE, request);
                                    } catch (Exception e) {
                                        log.error("[GITHUB][MQ] Failed to publish pull request message", e);
                                    }
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .then()
                .doOnSubscribe(sub ->
                        log.info("[GITHUB][MQ] Publishing {} pull requests to RabbitMQ", pullRequests.size())
                );
    }

    private Mono<Void> publishIssueMessages(List<GithubIssue> issues) {
        return Flux.fromIterable(issues)
                .flatMap(issue -> Mono.fromRunnable(() -> {
                                    try {
                                        GithubIssueRabbitRequest request = GithubIssueRabbitRequest.from(issue);
                                        rabbitTemplate.convertAndSend(GITHUB_ISSUE_QUEUE, request);
                                    } catch (Exception e) {
                                        log.error("[GITHUB][MQ] Failed to publish issue message", e);
                                    }
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .then()
                .doOnSubscribe(sub ->
                        log.info("[GITHUB][MQ] Publishing {} issues to RabbitMQ", issues.size())
                );
    }
}
