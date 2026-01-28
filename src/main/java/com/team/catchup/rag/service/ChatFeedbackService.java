package com.team.catchup.rag.service;

import com.team.catchup.rag.dto.client.ChatFeedbackRequest;
import com.team.catchup.rag.dto.client.ChatFeedbackResponse;
import com.team.catchup.rag.entity.ChatFeedback;
import com.team.catchup.rag.entity.ChatHistory;
import com.team.catchup.rag.repository.ChatFeedbackRepository;
import com.team.catchup.rag.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatFeedbackService {

    private final ChatFeedbackRepository chatFeedbackRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    @Transactional
    public ChatFeedbackResponse createFeedback(Long memberId, ChatFeedbackRequest request) {
        ChatHistory chatHistory = chatHistoryRepository.findById(request.chatHistoryId())
                .orElseThrow(() -> new IllegalArgumentException("[Feedback] Chat History Not Found"));

        if (!"assistant".equals(chatHistory.getRole())) {
            throw new IllegalArgumentException("[Feedback] User Query에 피드백 시도");
        }

        if (chatFeedbackRepository.existsByMemberIdAndChatHistoryId(memberId, request.chatHistoryId())) {
            throw new IllegalStateException("[Feedback] Feedback Exists for Chat History.");
        }

        ChatFeedback feedback = ChatFeedback.builder()
                .memberId(memberId)
                .chatHistory(chatHistory)
                .tags(request.tags())
                .detail(request.detail())
                .build();

        ChatFeedback saved = chatFeedbackRepository.save(feedback);

        return ChatFeedbackResponse.from(saved);
    }
}
