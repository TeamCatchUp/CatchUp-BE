package com.team.catchup.jira.repository;

import com.team.catchup.jira.entity.IssueType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueTypeRepository extends JpaRepository<IssueType, Integer> {
}
