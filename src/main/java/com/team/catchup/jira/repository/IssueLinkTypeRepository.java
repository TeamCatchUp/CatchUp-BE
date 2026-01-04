package com.team.catchup.jira.repository;

import com.team.catchup.jira.entity.IssueLinkType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueLinkTypeRepository extends JpaRepository<IssueLinkType, Integer> {
}
