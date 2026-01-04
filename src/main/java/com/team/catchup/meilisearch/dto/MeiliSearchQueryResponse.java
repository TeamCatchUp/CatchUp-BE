package com.team.catchup.meilisearch.dto;


import com.meilisearch.sdk.model.MultiSearchResult;
import com.meilisearch.sdk.model.Results;

/**
 * 검색 요청에 대해 Multi Search 기반 검색 결과를 반환하는 DTO
 * @param searchResult MeiliSearch 검색 결과
 */
public record MeiliSearchQueryResponse(
        Results<MultiSearchResult> searchResult
) {
    public static MeiliSearchQueryResponse from (Results<MultiSearchResult> searchResults) {
        return new MeiliSearchQueryResponse(searchResults);
    }
}
