package com.team.catchup.jira.repository;

import com.team.catchup.jira.entity.IssueMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueMetaDataRepository extends JpaRepository<IssueMetadata, Integer> {

    Optional<IssueMetadata> findByIssueKey(String issueKey);

    boolean existsByIssueKey(String issueKey);

    // 부모 이슈의 제목 찾기
    @Query("SELECT i.summary FROM IssueMetadata i WHERE i.issueId = :issueId")
    Optional<String> findSummaryByIssueId(@Param("issueId") Integer issueId);

    // 자식 이슈의 제목 찾기
    @Query("SELECT i.summary FROM IssueMetadata i WHERE i.parentIssueId = :myIssueId")
    List<String> findChildSummariesByParentId(@Param("myIssueId") Integer myIssueId);

}
