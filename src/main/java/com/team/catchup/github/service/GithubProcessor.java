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
    public GithubRepository processRepository(String owner, String repo, String branch) {
        log.info("[GITHUB][PROCESSOR] Processing repository: {}/{} on branch: {}", owner, repo, branch);

        try {
            return githubApiService.getRepository(owner, repo)
                    .map(repositoryMapper::toEntity)
                    .map(repository -> {
                        // targetBranch 설정
                        repository.updateSyncInfo(branch, GithubRepository.SyncStatus.IN_PROGRESS);
                        GithubRepository saved = persistenceService.saveRepository(repository);
                        publishRepositoryMessage(saved);
                        log.info("[GITHUB][PROCESSOR] Repository saved: {}/{} with branch: {}", owner, repo, branch);
                        return saved;
                    })
                    .block();
        } catch (Exception e) {
            log.error("[GITHUB][PROCESSOR] Failed to process repository: {}/{}", owner, repo, e);
            return null;
        }
    }

    /**
     * Commits 동기화 - branch 파라미터 추가 및 FileChange 생성
     */
    public SyncCount processCommits(GithubRepository repository, String owner, String repo, String branch, String since) {
        log.info("[GITHUB][PROCESSOR] Processing commits for {}/{} on branch: {}", owner, repo, branch);

        try {
            return githubApiService.getCommits(owner, repo, branch, since)
                    .flatMap(commitNode ->
                            githubApiService.getCommit(owner, repo, commitNode.get("sha").asText())
                    )
                    .collectList()
                    .map(commitDetailNodes -> {
                        List<GithubCommit> commits = new ArrayList<>();
                        List<GithubFileChange> allFileChanges = new ArrayList<>();

                        for (JsonNode detailNode : commitDetailNodes) {
                            // Commit 생성
                            GithubCommit commit = commitMapper.toEntity(detailNode, repository);
                            commits.add(commit);

                            // FileChange 생성 (files 배열 처리)
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

                        // 저장
                        int savedCommits = persistenceService.saveAllCommits(commits);
                        int savedFiles = persistenceService.saveAllFileChanges(allFileChanges);

                        log.info("[GITHUB][PROCESSOR] Commits saved - Commits: {}, FileChanges: {}",
                                savedCommits, savedFiles);

                        return SyncCount.of(commits.size(), savedCommits);
                    })
                    .block();
        } catch (Exception e) {
            log.error("[GITHUB][PROCESSOR] Failed to process commits for {}/{}", owner, repo, e);
            return SyncCount.empty();
        }
    }

    /**
     * Pull Requests 동기화 - branch(base) 파라미터 추가
     */
    public SyncCount processPullRequests(GithubRepository repository, String owner, String repo, String branch, String state, String since) {
        log.info("[GITHUB][PROCESSOR] Processing pull requests for {}/{} with base: {}", owner, repo, branch);

        try {
            return githubApiService.getPullRequests(owner, repo, branch, state, since)
                    .map(prNode -> pullRequestMapper.toEntity(prNode, repository))
                    .collectList()
                    .map(pullRequests -> {
                        int saved = persistenceService.saveAllPullRequests(pullRequests);
                        publishPullRequestMessages(pullRequests);
                        return SyncCount.of(pullRequests.size(), saved);
                    })
                    .block();
        } catch (Exception e) {
            log.error("[GITHUB][PROCESSOR] Failed to process pull requests for {}/{}", owner, repo, e);
            return SyncCount.empty();
        }
    }

    /**
     * Issues 동기화 (branch 무관)
     */
    public SyncCount processIssues(GithubRepository repository, String owner, String repo, String state, String since) {
        log.info("[GITHUB][PROCESSOR] Processing issues for {}/{}", owner, repo);

        try {
            return githubApiService.getIssues(owner, repo, state, since)
                    .filter(issueNode -> !issueNode.has("pull_request"))
                    .map(issueNode -> issueMapper.toEntity(issueNode, repository))
                    .collectList()
                    .map(issues -> {
                        int saved = persistenceService.saveAllIssues(issues);
                        publishIssueMessages(issues);
                        return SyncCount.of(issues.size(), saved);
                    })
                    .block();
        } catch (Exception e) {
            log.error("[GITHUB][PROCESSOR] Failed to process issues for {}/{}", owner, repo, e);
            return SyncCount.empty();
        }
    }

    /**
     * PR Reviews 동기화
     */
    public SyncCount processPullRequestReviews(String owner, String repo, int prNumber) {
        log.info("[GITHUB][PROCESSOR] Processing reviews for PR #{}", prNumber);

        GithubRepository repository = persistenceService.findRepositoryByOwnerAndName(owner, repo);
        if (repository == null) {
            log.error("[GITHUB][PROCESSOR] Repository not found: {}/{}", owner, repo);
            return SyncCount.empty();
        }

        GithubPullRequest pullRequest = persistenceService.findPullRequestByNumber(repository.getRepositoryId(), prNumber);
        if (pullRequest == null) {
            log.error("[GITHUB][PROCESSOR] Pull request not found: #{}", prNumber);
            return SyncCount.empty();
        }

        try {
            return githubApiService.getPullRequestReviews(owner, repo, prNumber)
                    .map(reviewNode -> reviewMapper.toEntity(reviewNode, repository, pullRequest))
                    .collectList()
                    .map(reviews -> {
                        int saved = persistenceService.saveAllReviews(reviews);
                        return SyncCount.of(reviews.size(), saved);
                    })
                    .block();
        } catch (Exception e) {
            log.error("[GITHUB][PROCESSOR] Failed to process reviews", e);
            return SyncCount.empty();
        }
    }

    /**
     * Issue Comments 동기화
     */
    public SyncCount processIssueComments(String owner, String repo, int issueNumber) {
        log.info("[GITHUB][PROCESSOR] Processing issue comments for #{}", issueNumber);

        GithubRepository repository = persistenceService.findRepositoryByOwnerAndName(owner, repo);
        if (repository == null) {
            log.error("[GITHUB][PROCESSOR] Repository not found: {}/{}", owner, repo);
            return SyncCount.empty();
        }

        GithubIssue issue = persistenceService.findIssueByNumber(repository.getRepositoryId(), issueNumber);
        if (issue == null) {
            log.error("[GITHUB][PROCESSOR] Issue not found: #{}", issueNumber);
            return SyncCount.empty();
        }

        try {
            return githubApiService.getIssueComments(owner, repo, issueNumber)
                    .map(commentNode -> commentMapper.toIssueCommentEntity(commentNode, repository, issue))
                    .collectList()
                    .map(comments -> {
                        int saved = persistenceService.saveAllComments(comments);
                        return SyncCount.of(comments.size(), saved);
                    })
                    .block();
        } catch (Exception e) {
            log.error("[GITHUB][PROCESSOR] Failed to process issue comments", e);
            return SyncCount.empty();
        }
    }

    /**
     * PR Review Comments 동기화
     */
    public SyncCount processPullRequestComments(String owner, String repo, int prNumber) {
        log.info("[GITHUB][PROCESSOR] Processing PR review comments for #{}", prNumber);

        GithubRepository repository = persistenceService.findRepositoryByOwnerAndName(owner, repo);
        if (repository == null) {
            log.error("[GITHUB][PROCESSOR] Repository not found: {}/{}", owner, repo);
            return SyncCount.empty();
        }

        GithubPullRequest pullRequest = persistenceService.findPullRequestByNumber(repository.getRepositoryId(), prNumber);
        if (pullRequest == null) {
            log.error("[GITHUB][PROCESSOR] Pull request not found: #{}", prNumber);
            return SyncCount.empty();
        }

        try {
            return githubApiService.getPullRequestReviewComments(owner, repo, prNumber)
                    .map(commentNode -> commentMapper.toReviewCommentEntity(commentNode, repository, pullRequest))
                    .collectList()
                    .map(comments -> {
                        int saved = persistenceService.saveAllComments(comments);
                        return SyncCount.of(comments.size(), saved);
                    })
                    .block();
        } catch (Exception e) {
            log.error("[GITHUB][PROCESSOR] Failed to process PR comments", e);
            return SyncCount.empty();
        }
    }

    /**
     * PR File Changes 동기화
     */
    public SyncCount processPullRequestFileChanges(String owner, String repo, int prNumber) {
        log.info("[GITHUB][PROCESSOR] Processing file changes for PR #{}", prNumber);

        GithubRepository repository = persistenceService.findRepositoryByOwnerAndName(owner, repo);
        if (repository == null) {
            log.error("[GITHUB][PROCESSOR] Repository not found: {}/{}", owner, repo);
            return SyncCount.empty();
        }

        GithubPullRequest pullRequest = persistenceService.findPullRequestByNumber(repository.getRepositoryId(), prNumber);
        if (pullRequest == null) {
            log.error("[GITHUB][PROCESSOR] Pull request not found: #{}", prNumber);
            return SyncCount.empty();
        }

        try {
            return githubApiService.getPullRequestFiles(owner, repo, prNumber)
                    .map(fileNode -> fileChangeMapper.toEntityFromPullRequest(fileNode, repository, pullRequest))
                    .collectList()
                    .map(fileChanges -> {
                        int saved = persistenceService.saveAllFileChanges(fileChanges);
                        return SyncCount.of(fileChanges.size(), saved);
                    })
                    .block();
        } catch (Exception e) {
            log.error("[GITHUB][PROCESSOR] Failed to process file changes", e);
            return SyncCount.empty();
        }
    }

    // ==================== RabbitMQ Publishing ====================

    private void publishRepositoryMessage(GithubRepository repository) {
        try {
            log.info("[GITHUB][MQ] Publishing repository to RabbitMQ: {}/{}",
                    repository.getOwner(), repository.getName());
            GithubRepositoryRabbitRequest request = GithubRepositoryRabbitRequest.from(repository);
            rabbitTemplate.convertAndSend(GITHUB_REPOSITORY_QUEUE, request);
        } catch (Exception e) {
            log.error("[GITHUB][MQ] Failed to publish repository message", e);
        }
    }

    private void publishPullRequestMessages(List<GithubPullRequest> pullRequests) {
        try {
            log.info("[GITHUB][MQ] Publishing {} pull requests to RabbitMQ", pullRequests.size());
            for (GithubPullRequest pr : pullRequests) {
                GithubPullRequestRabbitRequest request = GithubPullRequestRabbitRequest.from(pr);
                rabbitTemplate.convertAndSend(GITHUB_PULL_REQUEST_QUEUE, request);
            }
        } catch (Exception e) {
            log.error("[GITHUB][MQ] Failed to publish pull request messages", e);
        }
    }

    private void publishIssueMessages(List<GithubIssue> issues) {
        try {
            log.info("[GITHUB][MQ] Publishing {} issues to RabbitMQ", issues.size());
            for (GithubIssue issue : issues) {
                GithubIssueRabbitRequest request = GithubIssueRabbitRequest.from(issue);
                rabbitTemplate.convertAndSend(GITHUB_ISSUE_QUEUE, request);
            }
        } catch (Exception e) {
            log.error("[GITHUB][MQ] Failed to publish issue messages", e);
        }
    }
}