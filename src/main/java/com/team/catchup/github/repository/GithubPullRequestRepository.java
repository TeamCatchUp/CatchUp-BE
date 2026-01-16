package com.team.catchup.github.repository;

import com.team.catchup.github.entity.GithubPullRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubPullRequestRepository extends JpaRepository<GithubPullRequest, Long> {

    Optional<GithubPullRequest> findByRepository_RepositoryIdAndNumber(Long repositoryId, Integer number);

    List<GithubPullRequest> findByIndexedAtIsNull();

    List<GithubPullRequest> findByRepository_RepositoryIdAndIndexedAtIsNull(Long repositoryId);

    List<GithubPullRequest> findAllByPullRequestIdIn(List<Long> pullRequestIds);
}
