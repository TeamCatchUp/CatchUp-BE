package com.team.catchup.rag.repository;

import com.team.catchup.rag.entity.ChatFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, Long> {
    boolean existsByMemberIdAndChatHistoryId(Long memberId, Long chatHistoryId);
}
