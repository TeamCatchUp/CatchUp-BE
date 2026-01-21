package com.team.catchup.rag.repository;

import com.team.catchup.member.entity.Member;
import com.team.catchup.rag.entity.ChatHistory;
import com.team.catchup.rag.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    Slice<ChatHistory> findByChatRoom(ChatRoom chatRoom, Pageable pageable);
    Slice<ChatHistory> findByChatRoomAndRole(ChatRoom chatRoom, String role, Pageable pageable);
    Slice<ChatHistory> findByMemberAndRole(Member member, String role, Pageable pageable);
}
