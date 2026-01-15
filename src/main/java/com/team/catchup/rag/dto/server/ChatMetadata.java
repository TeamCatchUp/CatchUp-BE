package com.team.catchup.rag.dto.server;

import lombok.Builder;

import java.util.List;

/**
 * 채팅 히스토리 저장에 사용됨.
 */

@Builder
public record ChatMetadata(
        List<String> indexList,
        List<ServerSource> serverSources,
        Double processTime
) {
}
