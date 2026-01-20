package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

/**
 * DB 저장 시에 사용되는 FastAPI -> Spring 응답 데이터
 */

@Builder
public record ServerChatResponse(
        @NotBlank String answer,

        List<ServerSource> sources,

        @JsonProperty("process_time")
        Double processTime
) {
    public static ServerChatResponse createError(String errorMessage) {
        return ServerChatResponse.builder()
                .answer(errorMessage)
                .sources(List.of())
                .build();
    }

    /**
     * RAG 서버의 최종 답변과 출처를 정제하기 위한 정적 팩토리 함수
     */
    public static ServerChatResponse from(FastApiStreamingResponse dto) {
        return ServerChatResponse.builder()
                .answer(dto.getAnswer())
                .sources(dto.getSources())
                .processTime(dto.getProcessTime())
                .build();
    }
}