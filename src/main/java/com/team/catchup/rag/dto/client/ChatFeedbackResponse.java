package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.entity.ChatFeedback;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ChatFeedbackResponse(
        Long feedbackId,
        Long chatHistoryId,
        List<String> tags,
        String detail,
        LocalDateTime createdAt
) {
    public static ChatFeedbackResponse from(ChatFeedback feedback) {
        return ChatFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .chatHistoryId(feedback.getChatHistory().getId())
                .tags(feedback.getTags())
                .detail(feedback.getDetail())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
