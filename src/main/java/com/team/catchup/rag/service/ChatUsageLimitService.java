package com.team.catchup.rag.service;

import com.team.catchup.rag.dto.user.UserChatResponse;
import com.team.catchup.rag.entity.ChatUsageLimit;
import com.team.catchup.rag.repository.ChatUsageLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatUsageLimitService {

    private final ChatUsageLimitRepository chatUsageLimitRepository;

    @Value("${app.daily-chat-limit}")
    private int DAILY_CHAT_LIMIT;

    @Transactional
    public UserChatResponse checkAndIncrementUsageLimit(Long memberId, UUID sessionId) {
        ChatUsageLimit usageLimit = chatUsageLimitRepository
                .findByMemberIdAndUsageDate(memberId, LocalDate.now())
                .orElse(null);

        if (usageLimit != null && usageLimit.getUsageCount() >= DAILY_CHAT_LIMIT) {
            return UserChatResponse.of(sessionId, "일일 최대 채팅 횟수를 초과했습니다");
        }

        if (usageLimit == null) {
            usageLimit = ChatUsageLimit.createNewUsage(memberId);
        } else {
            usageLimit.incrementUsageCount();
        }
        chatUsageLimitRepository.save(usageLimit);

        return null;
    }
}
