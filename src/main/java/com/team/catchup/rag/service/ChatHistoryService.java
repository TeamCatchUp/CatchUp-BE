package com.team.catchup.rag.service;

import com.team.catchup.member.entity.Member;
import com.team.catchup.rag.dto.client.ChatHistoryResponse;
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
        ChatRoom chatRoom = chatRoomRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 없습니다."));

        if (!chatRoom.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("해당 채팅방에 접근 권한이 없습니다.");
        }

        return chatHistoryRepository.findByChatRoom(chatRoom, pageable)
                .map(ChatHistoryResponse::from);
    }
}
