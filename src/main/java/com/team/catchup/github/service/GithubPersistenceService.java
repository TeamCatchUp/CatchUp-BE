package com.team.catchup.github.service;

import com.team.catchup.github.entity.*;
import com.team.catchup.github.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubPersistenceService {

    private final TransactionTemplate transactionTemplate;

    private final GithubRepositoryRepository repositoryRepository;
    private final GithubCommitRepository commitRepository;
    private final GithubPullRequestRepository pullRequestRepository;
    private final GithubIssueRepository issueRepository;
    private final GithubCommentRepository commentRepository;
    private final GithubReviewRepository reviewRepository;
    private final GithubFileChangeRepository fileChangeRepository;

    // ==================== Repository ====================

    public Mono<GithubRepository> saveRepository(GithubRepository repository) {
        // 로깅은 트랜잭션 밖(호출 시점)에 남겨두거나 안으로 옮길 수 있으나,
        // 원본 로직 순서를 존중하여 로깅 후 비동기 작업 시작
        log.info("[GITHUB][PERSISTENCE] Saving repository: {}/{}",
                repository.getOwner(), repository.getName());

        return Mono.fromCallable(() ->
                transactionTemplate.execute(status ->
                        repositoryRepository.save(repository)
                )
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<GithubRepository> findRepository(String owner, String name) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status ->
                        repositoryRepository.findByOwnerAndName(owner, name).orElse(null)
                )
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Commits ====================

    public Mono<Integer> saveAllCommits(List<GithubCommit> commits) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status -> {
                    if (commits.isEmpty()) return 0;

                    List<String> shas = commits.stream()
                            .map(GithubCommit::getSha)
                            .distinct()
                            .toList();

                    Set<String> existingShas = commitRepository.findAllByShaIn(shas).stream()
                            .map(GithubCommit::getSha)
                            .collect(Collectors.toSet());

                    List<GithubCommit> newCommits = commits.stream()
                            .filter(c -> !existingShas.contains(c.getSha()))
                            .collect(Collectors.toMap(GithubCommit::getSha, c -> c, (p1, p2) -> p1))
                            .values().stream().toList();

                    if(!newCommits.isEmpty()) {
                        commitRepository.saveAll(newCommits);
                    }

                    log.info("[GITHUB][PERSISTENCE] Saved {}/{} commits", newCommits.size(), commits.size());
                    return newCommits.size();
                })
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Pull Requests ====================

    public Mono<Integer> saveAllPullRequests(List<GithubPullRequest> pullRequests) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status -> {
                    if (pullRequests.isEmpty()) return 0;

                    List<Long> prIds = pullRequests.stream()
                            .map(GithubPullRequest::getPullRequestId)
                            .distinct()
                            .collect(Collectors.toList());

                    Set<Long> existingIds = pullRequestRepository.findAllByPullRequestIdIn(prIds).stream()
                            .map(GithubPullRequest::getPullRequestId)
                            .collect(Collectors.toSet());

                    List<GithubPullRequest> newPullRequests = pullRequests.stream()
                            .filter(pr -> !existingIds.contains(pr.getPullRequestId()))
                            .collect(Collectors.toMap(GithubPullRequest::getPullRequestId, pr -> pr, (p1, p2) -> p1))
                            .values().stream().toList();

                    if (!newPullRequests.isEmpty()) {
                        pullRequestRepository.saveAll(newPullRequests);
                    }

                    log.info("[GITHUB][PERSISTENCE] Saved {}/{} pull requests", newPullRequests.size(), pullRequests.size());
                    return newPullRequests.size();
                })
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<GithubPullRequest>> findUnindexedPullRequests(Long repositoryId) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status -> {
                    if (repositoryId != null) {
                        return pullRequestRepository.findByRepository_RepositoryIdAndIndexedAtIsNull(repositoryId);
                    }
                    return pullRequestRepository.findByIndexedAtIsNull();
                })
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<GithubPullRequest> findPullRequestByNumber(Long repositoryId, Integer number) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status ->
                        pullRequestRepository.findByRepository_RepositoryIdAndNumber(repositoryId, number).orElse(null)
                )
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Issues ====================

    public Mono<Integer> saveAllIssues(List<GithubIssue> issues) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status -> {
                    if (issues.isEmpty()) return 0;

                    List<Long> issueIds = issues.stream()
                            .map(GithubIssue::getIssueId)
                            .distinct()
                            .collect(Collectors.toList());

                    Set<Long> existingIds = issueRepository.findAllByIssueIdIn(issueIds).stream()
                            .map(GithubIssue::getIssueId)
                            .collect(Collectors.toSet());

                    List<GithubIssue> newIssues = issues.stream()
                            .filter(issue -> !existingIds.contains(issue.getIssueId()))
                            .collect(Collectors.toMap(GithubIssue::getIssueId, i -> i, (i1, i2) -> i1))
                            .values().stream().toList();

                    if (!newIssues.isEmpty()) {
                        issueRepository.saveAll(newIssues);
                    }

                    log.info("[GITHUB][PERSISTENCE] Saved {}/{} issues", newIssues.size(), issues.size());
                    return newIssues.size();
                })
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<GithubIssue>> findUnindexedIssues(Long repositoryId) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status -> {
                    if (repositoryId != null) {
                        return issueRepository.findByRepository_RepositoryIdAndIndexedAtIsNull(repositoryId);
                    }
                    return issueRepository.findByIndexedAtIsNull();
                })
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<GithubIssue> findIssueByNumber(Long repositoryId, Integer number) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status ->
                        issueRepository.findByRepository_RepositoryIdAndNumber(repositoryId, number).orElse(null)
                )
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Comments ====================

    public Mono<Integer> saveAllComments(List<GithubComment> comments) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status -> {
                    if (comments.isEmpty()) return 0;

                    List<Long> commentIds = comments.stream()
                            .map(GithubComment::getCommentId)
                            .distinct()
                            .collect(Collectors.toList());

                    Set<Long> existingIds = commentRepository.findAllByCommentIdIn(commentIds).stream()
                            .map(GithubComment::getCommentId)
                            .collect(Collectors.toSet());

                    List<GithubComment> newComments = comments.stream()
                            .filter(comment -> !existingIds.contains(comment.getCommentId()))
                            .collect(Collectors.toMap(GithubComment::getCommentId, c -> c, (c1, c2) -> c1))
                            .values().stream().toList();

                    if (!newComments.isEmpty()) {
                        commentRepository.saveAll(newComments);
                    }

                    log.info("[GITHUB][PERSISTENCE] Saved {}/{} comments", newComments.size(), comments.size());
                    return newComments.size();
                })
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Reviews ====================

    public Mono<Integer> saveAllReviews(List<GithubReview> reviews) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status -> {
                    if (reviews.isEmpty()) return 0;

                    List<Long> reviewIds = reviews.stream()
                            .map(GithubReview::getReviewId)
                            .distinct()
                            .collect(Collectors.toList());

                    Set<Long> existingIds = reviewRepository.findAllByReviewIdIn(reviewIds).stream()
                            .map(GithubReview::getReviewId)
                            .collect(Collectors.toSet());

                    List<GithubReview> newReviews = reviews.stream()
                            .filter(review -> !existingIds.contains(review.getReviewId()))
                            .collect(Collectors.toMap(GithubReview::getReviewId, r -> r, (r1, r2) -> r1))
                            .values().stream().toList();

                    if (!newReviews.isEmpty()) {
                        reviewRepository.saveAll(newReviews);
                    }

                    log.info("[GITHUB][PERSISTENCE] Saved {}/{} reviews", newReviews.size(), reviews.size());
                    return newReviews.size();
                })
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== File Changes ====================

    public Mono<Integer> saveAllFileChanges(List<GithubFileChange> fileChanges) {
        return Mono.fromCallable(() ->
                transactionTemplate.execute(status -> {
                    fileChangeRepository.saveAll(fileChanges);
                    int savedCount = fileChanges.size();
                    log.info("[GITHUB][PERSISTENCE] Saved {} file changes", savedCount);
                    return savedCount;
                })
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Mark as Indexed ====================

    public Mono<Void> markPullRequestAsIndexed(Long pullRequestId) {
        return Mono.fromRunnable(() ->
                transactionTemplate.executeWithoutResult(status -> {
                    pullRequestRepository.findById(pullRequestId).ifPresent(pr -> {
                        pr.markAsIndexed();
                        pullRequestRepository.save(pr);
                    });
                    log.debug("[GITHUB][PERSISTENCE] Marked PR #{} as indexed", pullRequestId);
                })
        ).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> markIssueAsIndexed(Long issueId) {
        return Mono.fromRunnable(() ->
                transactionTemplate.executeWithoutResult(status -> {
                    issueRepository.findById(issueId).ifPresent(issue -> {
                        issue.markAsIndexed();
                        issueRepository.save(issue);
                    });
                    log.debug("[GITHUB][PERSISTENCE] Marked Issue #{} as indexed", issueId);
                })
        ).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
