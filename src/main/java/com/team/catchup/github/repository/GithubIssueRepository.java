package com.team.catchup.github.repository;

import com.team.catchup.github.entity.GithubIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubIssueRepository extends JpaRepository<GithubIssue, Long> {

    Optional<GithubIssue> findByRepository_RepositoryIdAndNumber(Long repositoryId, Integer number);

    List<GithubIssue> findByRepository_RepositoryId(Long repositoryId);

    List<GithubIssue> findAllByIssueIdIn(List<Long> issueIds);
}
