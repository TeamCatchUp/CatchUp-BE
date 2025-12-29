package com.team.catchup.common.sse.dto;

public record SseMessage<T>(
        SyncTarget target,
        SseEventType type,
        String message,
        T data
) {
    public static <T> SseMessage<T> of(
            SyncTarget target,
            SseEventType type,
            String message,
            T data
    ) {
        return new SseMessage<>(target, type, message, data);
    }

    public static SseMessage<Void> simple(
            SyncTarget target,
            SseEventType type,
            String message
    ) {
        return new SseMessage<>(target, type, message, null);
    }
}