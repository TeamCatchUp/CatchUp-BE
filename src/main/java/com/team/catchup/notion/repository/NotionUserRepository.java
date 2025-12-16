package com.team.catchup.notion.repository;

import com.team.catchup.notion.entity.NotionUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotionUserRepository extends JpaRepository<NotionUser, String> {
}
