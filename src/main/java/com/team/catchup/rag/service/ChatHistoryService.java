package com.team.catchup.rag.service;

import com.team.catchup.member.entity.Member;
import com.team.catchup.rag.dto.server.ServerChatResponse;
import com.team.catchup.rag.entity.ChatHistory;
import com.team.catchup.rag.entity.ChatRoom;
import com.team.catchup.rag.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {
    private final ChatHistoryRepository chatHistoryRepository;

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
}
