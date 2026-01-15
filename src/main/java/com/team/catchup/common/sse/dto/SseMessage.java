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

    /**
     * RAG 답변 생성 시 중간 과정 스트리밍을 위해 data를 포함하여 응답할 수 있도록 함.
     */
    public static <T> SseMessage<T> withData(
            SyncTarget target,
            SseEventType type,
            T data
    ) {
        return new SseMessage<>(target, type, null, data);
    }
}