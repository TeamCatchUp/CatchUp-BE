package com.team.catchup.rag.service;

import com.team.catchup.member.entity.Member;
import com.team.catchup.rag.dto.server.ServerChatResponse;
import com.team.catchup.rag.entity.ChatHistory;
import com.team.catchup.rag.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {
    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * 사용자 쿼리와 메타데이터를 DB에 저장한다.
     */
    public Mono<ChatHistory> saveUserQuery(Member member, UUID sessionId, String query, List<String> indexList) {
        return Mono.fromCallable(() -> {
            ChatHistory userLog = ChatHistory.createUserInfo(sessionId, member, query, indexList);
            return chatHistoryRepository.save(userLog);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 어시스턴트 응답과 메타데이터를 DB에 저장한다.
     */
    public Mono<ChatHistory> saveAssistantResponse(Member member, UUID sessionId, ServerChatResponse response) {
        return Mono.fromCallable(() -> {
            ChatHistory assistantLog = ChatHistory.createAssistantInfo(sessionId, member, response);
            return chatHistoryRepository.save(assistantLog);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
