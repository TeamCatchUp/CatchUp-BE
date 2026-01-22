package com.team.catchup.github.repository;

import com.team.catchup.github.entity.GithubCommit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubCommitRepository extends JpaRepository<GithubCommit, Long> {

    List<GithubCommit> findAllByShaIn(List<String> shas);

    List<GithubCommit> findByRepository_RepositoryIdAndShaIn(Long repositoryId, List<String> shas);

    @Query("SELECT c " +
            "FROM GithubCommit  c, GithubFileChange fc " +
            "WHERE c.sha = fc.commitSha " +
            "AND fc.filePath = :filePath " +
            "ORDER BY c.authorDate DESC")
    List<GithubCommit> findLatestByFilePath(@Param("filePath") String filePath, Pageable pageable);
}
