package com.team.catchup.rag.service;

import com.team.catchup.member.entity.Member;
import com.team.catchup.member.repository.MemberRepository;
import com.team.catchup.rag.dto.client.ChatRoomResponse;
import com.team.catchup.rag.entity.ChatRoom;
import com.team.catchup.rag.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    /**
     * 채팅방 조회 또는 생성
     */
    @Transactional
    public ChatRoom getOrCreateChatRoom(Member member, UUID sessionId, String initialQuery) {
        return chatRoomRepository.findById(sessionId)
                .orElseGet(() -> createChatRoom(member, sessionId, initialQuery));
    }

    /**
     * 채팅방 생성
     */
    private ChatRoom createChatRoom(Member member, UUID sessionId, String initialQuery) {
        String title = initialQuery.length() > 20 ? initialQuery.substring(0, 20) + "..." : initialQuery;
        ChatRoom chatRoom = ChatRoom.builder()
                .sessionId(sessionId)
                .member(member)
                .title(title)
                .build();

        return chatRoomRepository.save(chatRoom);
    }

    /**
     * 마지막 활동 시간 갱신
     */
    @Transactional
    public void updateLastActiveTime(UUID sessionId) {
        ChatRoom chatRoom = chatRoomRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 세션입니다."));

        chatRoom.updateLastActiveTime();
    }


    /**
     * 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRooms(Long memberId) {
        Member owner = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 채팅방 목록
        List<ChatRoom> chatRooms = chatRoomRepository.findByMemberOrderByUpdatedAtDesc(owner);

        return chatRooms.stream()
                .map(ChatRoomResponse::from)
                .toList();
    }
}
