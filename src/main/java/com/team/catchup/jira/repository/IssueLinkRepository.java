package com.team.catchup.jira.repository;

import com.team.catchup.jira.entity.IssueLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueLinkRepository extends JpaRepository<IssueLink, Integer> {
}
