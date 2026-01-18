package com.team.catchup.github.repository;

import com.team.catchup.github.entity.GithubCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubCommitRepository extends JpaRepository<GithubCommit, Long> {

    List<GithubCommit> findAllByShaIn(List<String> shas);

    List<GithubCommit> findByRepository_RepositoryIdAndShaIn(Long repositoryId, List<String> shas);
}
