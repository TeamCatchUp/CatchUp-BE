package com.team.catchup.github.repository;

import com.team.catchup.github.entity.GithubCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubCommitRepository extends JpaRepository<GithubCommit, Long> {

    Optional<GithubCommit> findBySha(String sha);

    boolean existsBySha(String sha);

    List<GithubCommit> findByIndexedAtIsNull();

    List<GithubCommit> findByRepository_RepositoryIdAndIndexedAtIsNull(Long repositoryId);
}
