package com.team.catchup.github.service;

import com.team.catchup.github.entity.*;
import com.team.catchup.github.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public GithubRepository saveRepository(GithubRepository repository) {
        log.info("[GITHUB][PERSISTENCE] Saving repository: {}/{}",
            repository.getOwner(), repository.getName());
        return repositoryRepository.save(repository);
    }

    @Transactional(readOnly = true)
    public GithubRepository findRepository(String owner, String name) {
        return repositoryRepository.findByOwnerAndName(owner, name)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public GithubRepository findRepositoryByOwnerAndName(String owner, String name) {
        return repositoryRepository.findByOwnerAndName(owner, name)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean repositoryExists(String owner, String name) {
        return repositoryRepository.existsByOwnerAndName(owner, name);
    }

    // ==================== Commits ====================

    @Transactional
    public int saveAllCommits(List<GithubCommit> commits) {
        int savedCount = 0;
        for (GithubCommit commit : commits) {
            if (saveCommitIfNotExists(commit)) {
                savedCount++;
            }
        }
        log.info("[GITHUB][PERSISTENCE] Saved {}/{} commits", savedCount, commits.size());
        return savedCount;
    }

    @Transactional
    public boolean saveCommitIfNotExists(GithubCommit commit) {
        if (commitRepository.existsBySha(commit.getSha())) {
            return false;
        }
        commitRepository.save(commit);
        return true;
    }

    @Transactional(readOnly = true)
    public List<GithubCommit> findUnindexedCommits(Long repositoryId) {
        if (repositoryId != null) {
            return commitRepository.findByRepository_RepositoryIdAndIndexedAtIsNull(repositoryId);
        }
        return commitRepository.findByIndexedAtIsNull();
    }

    // ==================== Pull Requests ====================

    @Transactional
    public int saveAllPullRequests(List<GithubPullRequest> pullRequests) {
        int savedCount = 0;
        for (GithubPullRequest pr : pullRequests) {
            if (savePullRequestIfNotExists(pr)) {
                savedCount++;
            }
        }
        log.info("[GITHUB][PERSISTENCE] Saved {}/{} pull requests", savedCount, pullRequests.size());
        return savedCount;
    }

    @Transactional
    public boolean savePullRequestIfNotExists(GithubPullRequest pullRequest) {
        if (pullRequestRepository.existsByPullRequestId(pullRequest.getPullRequestId())) {
            return false;
        }
        pullRequestRepository.save(pullRequest);
        return true;
    }

    @Transactional(readOnly = true)
    public List<GithubPullRequest> findUnindexedPullRequests(Long repositoryId) {
        if (repositoryId != null) {
            return pullRequestRepository.findByRepository_RepositoryIdAndIndexedAtIsNull(repositoryId);
        }
        return pullRequestRepository.findByIndexedAtIsNull();
    }

    @Transactional(readOnly = true)
    public GithubPullRequest findPullRequestByNumber(Long repositoryId, Integer number) {
        return pullRequestRepository.findByRepository_RepositoryIdAndNumber(repositoryId, number)
                .orElse(null);
    }

    // ==================== Issues ====================

    @Transactional
    public int saveAllIssues(List<GithubIssue> issues) {
        int savedCount = 0;
        for (GithubIssue issue : issues) {
            if (saveIssueIfNotExists(issue)) {
                savedCount++;
            }
        }
        log.info("[GITHUB][PERSISTENCE] Saved {}/{} issues", savedCount, issues.size());
        return savedCount;
    }

    @Transactional
    public boolean saveIssueIfNotExists(GithubIssue issue) {
        if (issueRepository.existsByIssueId(issue.getIssueId())) {
            return false;
        }
        issueRepository.save(issue);
        return true;
    }

    @Transactional(readOnly = true)
    public List<GithubIssue> findUnindexedIssues(Long repositoryId) {
        if (repositoryId != null) {
            return issueRepository.findByRepository_RepositoryIdAndIndexedAtIsNull(repositoryId);
        }
        return issueRepository.findByIndexedAtIsNull();
    }

    @Transactional(readOnly = true)
    public GithubIssue findIssueByNumber(Long repositoryId, Integer number) {
        return issueRepository.findByRepository_RepositoryIdAndNumber(repositoryId, number)
                .orElse(null);
    }

    // ==================== Comments ====================

    @Transactional
    public int saveAllComments(List<GithubComment> comments) {
        int savedCount = 0;
        for (GithubComment comment : comments) {
            if (saveCommentIfNotExists(comment)) {
                savedCount++;
            }
        }
        log.info("[GITHUB][PERSISTENCE] Saved {}/{} comments", savedCount, comments.size());
        return savedCount;
    }

    @Transactional
    public boolean saveCommentIfNotExists(GithubComment comment) {
        if (commentRepository.existsByCommentId(comment.getCommentId())) {
            return false;
        }
        commentRepository.save(comment);
        return true;
    }

    @Transactional(readOnly = true)
    public List<GithubComment> findUnindexedComments(Long repositoryId) {
        if (repositoryId != null) {
            return commentRepository.findByRepository_RepositoryIdAndIndexedAtIsNull(repositoryId);
        }
        return commentRepository.findByIndexedAtIsNull();
    }

    // ==================== Reviews ====================

    @Transactional
    public int saveAllReviews(List<GithubReview> reviews) {
        int savedCount = 0;
        for (GithubReview review : reviews) {
            if (saveReviewIfNotExists(review)) {
                savedCount++;
            }
        }
        log.info("[GITHUB][PERSISTENCE] Saved {}/{} reviews", savedCount, reviews.size());
        return savedCount;
    }

    @Transactional
    public boolean saveReviewIfNotExists(GithubReview review) {
        if (reviewRepository.existsByReviewId(review.getReviewId())) {
            return false;
        }
        reviewRepository.save(review);
        return true;
    }

    @Transactional(readOnly = true)
    public List<GithubReview> findUnindexedReviews(Long repositoryId) {
        if (repositoryId != null) {
            return reviewRepository.findByRepository_RepositoryIdAndIndexedAtIsNull(repositoryId);
        }
        return reviewRepository.findByIndexedAtIsNull();
    }

    // ==================== File Changes ====================

    @Transactional
    public int saveAllFileChanges(List<GithubFileChange> fileChanges) {
        int savedCount = fileChanges.size();
        fileChangeRepository.saveAll(fileChanges);
        log.info("[GITHUB][PERSISTENCE] Saved {} file changes", savedCount);
        return savedCount;
    }

    @Transactional(readOnly = true)
    public List<GithubFileChange> findFileChangesByCommit(String commitSha) {
        return fileChangeRepository.findByCommitSha(commitSha);
    }

    @Transactional(readOnly = true)
    public List<GithubFileChange> findFileChangesByPullRequest(Long pullRequestId) {
        return fileChangeRepository.findByPullRequest_PullRequestId(pullRequestId);
    }
}
