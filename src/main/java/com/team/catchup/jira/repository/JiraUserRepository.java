package com.team.catchup.jira.repository;

import com.team.catchup.jira.entity.JiraUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraUserRepository extends JpaRepository<JiraUser, String> {
}
