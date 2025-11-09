package com.team.catchup.meilisearch.service;

import com.meilisearch.sdk.*;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.json.GsonJsonHandler;
import com.meilisearch.sdk.model.MultiSearchResult;
import com.meilisearch.sdk.model.Results;
import com.team.catchup.meilisearch.document.MeiliSearchDocument;
import com.team.catchup.meilisearch.dto.MeiliSearchQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MeiliSearchService
 * Document 생성, Multi-Search 검색
 */
@Service
@RequiredArgsConstructor
public class MeiliSearchService {
    private final Client meiliSearchClient;
    private final GsonJsonHandler jsonHandler = new GsonJsonHandler();

    /**
     * Document 생성 또는 갱신
     * MeiliSearch는 새로운 Document는 생성하고 이미 존재하는 Document는 덮어쓴다.
     *
     * @param documents MeiliSearchDocument 구현체의 리스트, 동일한 구현체로 이루어져있어야 한다
     */
    public void addOrUpdateDocument(List<MeiliSearchDocument> documents) {
        if (documents == null || documents.isEmpty()) return;

        String indexName = documents.stream()
                .map(MeiliSearchDocument::getIndexName)
                .distinct()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("indexName을 결정할 수 없습니다."));

        try {
            String documentsJson = jsonHandler.encode(documents);
            Index index = meiliSearchClient.index(indexName);
            index.addDocuments(documentsJson);
        } catch (MeilisearchException e){
            throw new RuntimeException("Document 생성(갱신)에 실패했습니다.", e);
        }
    }

    /**
     * Multi-Search 기반 MeiliSearch 검색
     * MeiliSearch는 기본적으로 하나의 index에 대한 검색만 수행하지만, 한 번의 쿼리로 여러 index애 대한 검색이 가능하도록
     * multi-search 기능을 지원한다. 이를 통해 여러 협업 툴 각각의 index에 대해 검색을 수행할 수 있다.
     *
     * @param query 사용자가 입력한 검색 쿼리
     * @param indices 검색 대상 indexName을 담은 리스트
     *
     * @return MultiSearchResult을 MeiliSearchQueryResponse로 변환한 객체를 반환한다
     */
    public MeiliSearchQueryResponse search(String query, List<String> indices) {
        try {
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

            for (String indexName : indices) {
                IndexSearchRequest request = new IndexSearchRequest(indexName)
                        .setQuery(query);

                multiSearchRequest.addQuery(request);
            }

            Results<MultiSearchResult> results = meiliSearchClient.multiSearch(multiSearchRequest);

            return MeiliSearchQueryResponse.from(results);
        } catch (MeilisearchException e) {
            throw new MeilisearchException(e.getMessage());
        }
    }

}
