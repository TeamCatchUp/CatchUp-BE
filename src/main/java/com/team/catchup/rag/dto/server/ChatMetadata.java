package com.team.catchup.rag.dto.server;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatMetadata(
        List<String> indexList,
        List<ServerSource> serverSources,
        Double processTime
) {
}
