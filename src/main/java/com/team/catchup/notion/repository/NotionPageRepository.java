package com.team.catchup.notion.repository;

import com.team.catchup.notion.entity.NotionPage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotionPageRepository extends JpaRepository<NotionPage, String> {
}
