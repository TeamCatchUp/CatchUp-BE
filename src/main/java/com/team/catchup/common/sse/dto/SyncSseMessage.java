package com.team.catchup.common.sse.dto;

public record SyncSseMessage<T>(
        MessageType messageType,
        SseEventType type,
        String message,
        T data
) {
    public static <T> SyncSseMessage<T> of(
            MessageType target,
            SseEventType type,
            String message,
            T data
    ) {
        return new SyncSseMessage<>(target, type, message, data);
    }

    public static SyncSseMessage<Void> simple(
            MessageType target,
            SseEventType type,
            String message
    ) {
        return new SyncSseMessage<>(target, type, message, null);
    }
}