package com.team.catchup.rag.service;

import com.team.catchup.member.entity.Member;
import com.team.catchup.member.repository.MemberRepository;
import com.team.catchup.rag.dto.client.ChatHistoryResponse;
import com.team.catchup.rag.dto.client.UserQueryHistoryResponse;
import com.team.catchup.rag.dto.server.ServerChatResponse;
import com.team.catchup.rag.entity.ChatHistory;
import com.team.catchup.rag.entity.ChatRoom;
import com.team.catchup.rag.repository.ChatHistoryRepository;
import com.team.catchup.rag.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    /**
     * 사용자 쿼리와 메타데이터를 DB에 저장한다.
     */
    @Transactional
    public void saveUserQuery(Member member, ChatRoom chatRoom, String query, List<String> indexList) {
        ChatHistory userLog = ChatHistory.createUserInfo(chatRoom, member, query, indexList);
        chatHistoryRepository.save(userLog);
    }

    /**
     * 어시스턴트 응답과 메타데이터를 DB에 저장한다.
     */
    @Transactional
    public void saveAssistantResponse(Member member, ChatRoom chatRoom, ServerChatResponse response) {
        ChatHistory assistantLog = ChatHistory.createAssistantInfo(chatRoom, member, response);
        chatHistoryRepository.save(assistantLog);
    }

    /**
     * 채팅 히스토리 조회
     */
    @Transactional(readOnly = true)
    public Slice<ChatHistoryResponse> getChatHistory(UUID sessionId, Long memberId, Pageable pageable) {
        ChatRoom chatRoom = getChatRoom(sessionId);
        validateMember(chatRoom, memberId);
        return chatHistoryRepository.findByChatRoom(chatRoom, pageable)
                .map(ChatHistoryResponse::from);
    }

    /**
     * 특정 채팅방에서 사용자가 남긴 쿼리 목록
     */
    @Transactional(readOnly = true)
    public Slice<UserQueryHistoryResponse> getUserQueryHistories(UUID sessionId, Long memberId, Pageable pageable) {
        ChatRoom chatRoom = getChatRoom(sessionId);
        validateMember(chatRoom, memberId);

        return chatHistoryRepository.findByChatRoomAndRole(chatRoom, "user", pageable)
                .map(UserQueryHistoryResponse::from);
    }

    /**
     * 특정 유저가 남긴 모든 쿼리 목록
     */
    @Transactional(readOnly = true)
    public Slice<UserQueryHistoryResponse> getAllUserQueryHistories(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return chatHistoryRepository.findByMemberAndRole(member, "user", pageable)
                .map(UserQueryHistoryResponse::from);
    }

    /**
     * sessionId에 해당하는 ChatRoom 반환 헬퍼 함수
     */
    private ChatRoom getChatRoom(UUID sessionId) {
        return chatRoomRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 없습니다."));
    }

    /**
     * ChatRoom에 대한 Member의 접근 권한 확인
     */
    private void validateMember(ChatRoom chatRoom, Long memberId) {
        if (!chatRoom.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("해당 채팅방에 접근 권한이 없습니다.");
        }
    }
}
