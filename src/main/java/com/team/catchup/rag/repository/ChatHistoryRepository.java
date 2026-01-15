package com.team.catchup.rag.repository;

import com.team.catchup.rag.entity.ChatHistory;
import com.team.catchup.rag.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    Page<ChatHistory> findByChatRoom(ChatRoom chatRoom, Pageable pageable);
}
