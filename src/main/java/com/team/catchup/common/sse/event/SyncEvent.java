package com.team.catchup.common.sse.event;

import com.team.catchup.common.sse.dto.SseEventType;
import com.team.catchup.common.sse.dto.SseMessage;
import com.team.catchup.common.sse.dto.SyncTarget;
import lombok.Getter;

@Getter
public class SyncEvent {
    private final String userId;
    private final SseMessage<?> message;

    public SyncEvent(String userId, SyncTarget target, SseEventType type, String simpleMessage) {
        this.userId = userId;
        this.message = SseMessage.simple(target, type, simpleMessage);
    }

    public <T> SyncEvent(String userId, SyncTarget target, SseEventType type, String messageText, T data) {
        this.userId = userId;
        this.message = SseMessage.of(target, type, messageText, data);
    }
}