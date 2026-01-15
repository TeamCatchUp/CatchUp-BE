package com.team.catchup.common.sse.dto;

public enum SseEventType {
    CONNECT,
    IN_PROGRESS,
    COMPLETED,
    FAILED,

    // RAG 답변 생성 시 중간 과정 스트리밍 관련
    RAG_IN_PROGRESS,
    RAG_DONE
}
