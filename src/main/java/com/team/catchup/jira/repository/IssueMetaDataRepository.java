package com.team.catchup.jira.repository;

import com.team.catchup.jira.entity.IssueMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IssueMetaDataRepository extends JpaRepository<IssueMetadata, Integer> {

    Optional<IssueMetadata> findByIssueKey(String issueKey);

    boolean existsByIssueKey(String issueKey);
}
