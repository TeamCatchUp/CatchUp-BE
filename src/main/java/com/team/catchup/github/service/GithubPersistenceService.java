package com.team.catchup.github.service;

import com.team.catchup.github.entity.*;
import com.team.catchup.github.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubPersistenceService {

    private final GithubRepositoryRepository repositoryRepository;
    private final GithubCommitRepository commitRepository;
    private final GithubPullRequestRepository pullRequestRepository;
    private final GithubIssueRepository issueRepository;
    private final GithubCommentRepository commentRepository;
    private final GithubReviewRepository reviewRepository;
    private final GithubFileChangeRepository fileChangeRepository;

    // ==================== Repository ====================

    @Transactional
    public Mono<GithubRepository> saveRepository(GithubRepository repository) {
        log.info("[GITHUB][PERSISTENCE] Saving repository: {}/{}",
            repository.getOwner(), repository.getName());
        return Mono.fromCallable(() -> repositoryRepository.save(repository))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<GithubRepository> findRepository(String owner, String name) {
        return Mono.fromCallable(() -> repositoryRepository.findByOwnerAndName(owner, name).orElse(null))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Commits ====================

    @Transactional
    public Mono<Integer> saveAllCommits(List<GithubCommit> commits) {
        return Mono.fromCallable(() -> {
            int savedCount = 0;
            for(GithubCommit commit : commits) {
                if(!commitRepository.existsBySha(commit.getSha())) {
                    commitRepository.save(commit);
                    savedCount++;
                }
            }
            log.info("[GITHUB][PERSISTENCE] Saved {}/{} commits", savedCount, commits.size());
            return savedCount;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Pull Requests ====================

    @Transactional
    public Mono<Integer> saveAllPullRequests(List<GithubPullRequest> pullRequests) {
        return Mono.fromCallable(() -> {
            int savedCount = 0;
            for (GithubPullRequest pr : pullRequests) {
                if (!pullRequestRepository.existsByPullRequestId(pr.getPullRequestId())) {
                    pullRequestRepository.save(pr);
                    savedCount++;
                }
            }
            log.info("[GITHUB][PERSISTENCE] Saved {}/{} pull requests", savedCount, pullRequests.size());
            return savedCount;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<List<GithubPullRequest>> findUnindexedPullRequests(Long repositoryId) {
        return Mono.fromCallable(() -> {
            if (repositoryId != null) {
                return pullRequestRepository.findByRepository_RepositoryIdAndIndexedAtIsNull(repositoryId);
            }
            return pullRequestRepository.findByIndexedAtIsNull();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<GithubPullRequest> findPullRequestByNumber(Long repositoryId, Integer number) {
        return Mono.fromCallable(() ->
                pullRequestRepository.findByRepository_RepositoryIdAndNumber(repositoryId, number).orElse(null)
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Issues ====================

    @Transactional
    public Mono<Integer> saveAllIssues(List<GithubIssue> issues) {
        return Mono.fromCallable(() -> {
            int savedCount = 0;
            for (GithubIssue issue : issues) {
                if (!issueRepository.existsByIssueId(issue.getIssueId())) {
                    issueRepository.save(issue);
                    savedCount++;
                }
            }
            log.info("[GITHUB][PERSISTENCE] Saved {}/{} issues", savedCount, issues.size());
            return savedCount;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<List<GithubIssue>> findUnindexedIssues(Long repositoryId) {
        return Mono.fromCallable(() -> {
            if (repositoryId != null) {
                return issueRepository.findByRepository_RepositoryIdAndIndexedAtIsNull(repositoryId);
            }
            return issueRepository.findByIndexedAtIsNull();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional(readOnly = true)
    public Mono<GithubIssue> findIssueByNumber(Long repositoryId, Integer number) {
        return Mono.fromCallable(() ->
                issueRepository.findByRepository_RepositoryIdAndNumber(repositoryId, number).orElse(null)
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Comments ====================

    @Transactional
    public Mono<Integer> saveAllComments(List<GithubComment> comments) {
        return Mono.fromCallable(() -> {
            int savedCount = 0;
            for (GithubComment comment : comments) {
                if (!commentRepository.existsByCommentId(comment.getCommentId())) {
                    commentRepository.save(comment);
                    savedCount++;
                }
            }
            log.info("[GITHUB][PERSISTENCE] Saved {}/{} comments", savedCount, comments.size());
            return savedCount;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Reviews ====================

    @Transactional
    public Mono<Integer> saveAllReviews(List<GithubReview> reviews) {
        return Mono.fromCallable(() -> {
            int savedCount = 0;
            for (GithubReview review : reviews) {
                if (!reviewRepository.existsByReviewId(review.getReviewId())) {
                    reviewRepository.save(review);
                    savedCount++;
                }
            }
            log.info("[GITHUB][PERSISTENCE] Saved {}/{} reviews", savedCount, reviews.size());
            return savedCount;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== File Changes ====================

    @Transactional
    public Mono<Integer> saveAllFileChanges(List<GithubFileChange> fileChanges) {
        return Mono.fromCallable(() -> {
            fileChangeRepository.saveAll(fileChanges);
            int savedCount = fileChanges.size();
            log.info("[GITHUB][PERSISTENCE] Saved {} file changes", savedCount);
            return savedCount;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
