package com.team.catchup.rag.repository;

import com.team.catchup.member.entity.Member;
import com.team.catchup.rag.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Slice<ChatRoom> findByMember(Member member, Pageable pageable);
    ChatRoom findBySessionId(UUID sessionId);
}
