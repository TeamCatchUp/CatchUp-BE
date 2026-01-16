package com.team.catchup.rag.repository;

import com.team.catchup.member.entity.Member;
import com.team.catchup.rag.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    List<ChatRoom> findByMemberOrderByUpdatedAtDesc(Member member);
    ChatRoom findBySessionId(UUID sessionId);
}
