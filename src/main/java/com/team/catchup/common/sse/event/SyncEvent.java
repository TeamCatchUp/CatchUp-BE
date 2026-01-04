package com.team.catchup.common.sse.event;

import com.team.catchup.common.sse.dto.SseEventType;
import com.team.catchup.common.sse.dto.SyncSseMessage;
import com.team.catchup.common.sse.dto.MessageType;
import lombok.Getter;

@Getter
public class SyncEvent {
    private final Long userId;
    private final SyncSseMessage<?> message;

    public SyncEvent(Long userId, MessageType target, SseEventType type, String simpleMessage) {
        this.userId = userId;
        this.message = SyncSseMessage.simple(target, type, simpleMessage);
    }

    public <T> SyncEvent(Long userId, MessageType target, SseEventType type, String messageText, T data) {
        this.userId = userId;
        this.message = SyncSseMessage.of(target, type, messageText, data);
    }
}