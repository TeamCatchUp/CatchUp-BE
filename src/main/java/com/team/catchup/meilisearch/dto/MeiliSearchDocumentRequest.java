package com.team.catchup.meilisearch.dto;

import com.team.catchup.meilisearch.document.MeiliSearchDocument;

import java.util.List;

/**
 * Document 생성 및 갱신 요청에 사용하는 DTO
 * @param documents MeiliSearchDocument의 구현체
 */
public record MeiliSearchDocumentRequest(
        List<MeiliSearchDocument> documents
) {
}
