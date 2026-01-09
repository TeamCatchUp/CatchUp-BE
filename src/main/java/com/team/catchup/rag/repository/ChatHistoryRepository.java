package com.team.catchup.rag.repository;

import com.team.catchup.rag.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

}
