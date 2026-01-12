package com.team.catchup.rag.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ChatMetadata(
        List<String> indexList,
        List<Source> sources,
        Double processTime
) {
}
