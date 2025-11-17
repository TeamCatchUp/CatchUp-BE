package com.team.catchup.meilisearch.service;

import com.meilisearch.sdk.*;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.json.GsonJsonHandler;
import com.meilisearch.sdk.model.MultiSearchResult;
import com.meilisearch.sdk.model.Results;
import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import com.team.catchup.meilisearch.document.JiraIssueDocument;
import com.team.catchup.meilisearch.document.MeiliSearchDocument;
import com.team.catchup.meilisearch.dto.MeiliSearchQueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MeiliSearchService
 * Document 생성, Multi-Search 검색
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeiliSearchService {
    private final Client meiliSearchClient;
    private final GsonJsonHandler jsonHandler = new GsonJsonHandler();

    /**
     * Create or update MeiliSearch documents, overwriting any existing documents.
     *
     * @param documents list of MeiliSearchDocument instances to add or update; if `null` or empty the method returns without action
     * @throws RuntimeException if adding/updating documents for an index fails due to a Meilisearch error
     */
    public void addOrUpdateDocument(List<MeiliSearchDocument> documents) {
        if (documents == null || documents.isEmpty()) return;

        Map<String, List<MeiliSearchDocument>> groupedByIndex = documents.stream()
                .collect(Collectors.groupingBy(MeiliSearchDocument::getIndexName));

        groupedByIndex.forEach((indexName, docs) ->{
            try {
                String documentsJson = jsonHandler.encode(docs);
                Index index = meiliSearchClient.index(indexName);
                log.info("MeiliSearchService][addOrUpdateDocument][indexName: {}, docs: {}", indexName, documentsJson);
                index.addDocuments(documentsJson);
            } catch (MeilisearchException e) {
                throw new RuntimeException("[" + indexName + "] 인덱스 문서 추가/갱신 실패", e);
            }
        });
    }

    /**
     * Search multiple MeiliSearch indices for the given query using the multi-search API.
     *
     * @param query   the search query string
     * @param indices the list of MeiliSearch index names to query
     * @return a MeiliSearchQueryResponse containing the multi-search results; returns an empty result when the query is blank or no indices are provided
     * @throws RuntimeException if the MeiliSearch client fails while executing the multi-search
     */
    public MeiliSearchQueryResponse search(String query, List<String> indices) {
        if (query == null || query.isBlank() || CollectionUtils.isEmpty(indices)) {
            return MeiliSearchQueryResponse.from(new Results<>());
        }

        try {
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

            for (String indexName : indices) {
                IndexSearchRequest request = new IndexSearchRequest(indexName)
                        .setQuery(query);

                multiSearchRequest.addQuery(request);
            }

            log.info("[MeiliSearchService][search] 검색 시도 - query: {}, indices: {}", query, indices);

            Results<MultiSearchResult> results = meiliSearchClient.multiSearch(multiSearchRequest);

            log.info("[MeiliSearchService][search] 검색 결과 - results: {}", results);

            return MeiliSearchQueryResponse.from(results);

        } catch (MeilisearchException e) {

            throw new RuntimeException("MeiliSearch 검색 실패: " + e.getMessage(), e);

        }
    }

    /**
     * Convert a Jira IssueMetaDataResponse into a list of MeiliSearch documents.
     *
     * Converts each Jira issue in the response into a MeiliSearchDocument implementation.
     *
     * @param response the Jira IssueMetaDataResponse containing issues to convert
     * @return a list of MeiliSearchDocument implementations corresponding to the response's issues
     */
    public List<MeiliSearchDocument> createDocuments (IssueMetaDataResponse response) {
        // TODO: API 응답 출처에 따라 유연하게 문서를 생성하도록 수정. 현재는 Jira의 IssueMetaDataResponse만 지원.
        return response.issues().stream()
                .map(JiraIssueDocument::from)
                .collect(Collectors.toList());
    }

}