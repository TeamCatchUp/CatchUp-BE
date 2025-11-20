package com.team.catchup.jira.repository;

import com.team.catchup.jira.entity.JiraProject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraProjectRepository extends JpaRepository<JiraProject, Integer> {
}
