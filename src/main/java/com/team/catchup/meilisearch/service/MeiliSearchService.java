package com.team.catchup.meilisearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
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
    //private final GsonJsonHandler jsonHandler = new GsonJsonHandler();

    private final ObjectMapper objectMapper;

    /**
     * MeiliSearch Document 생성 또는 갱신.
     * 새로운 Document를 생성하고 이미 존재하는 Document라면 덮어쓴다.
     * 예외가 발생할 경우 최대 3번까지 재시도하며, 재시도 간격은 delay * (multiplie ^ 실패 횟수)이다.
     *
     * @param documents MeiliSearchDocument 구현체의 리스트
     */
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void addOrUpdateDocument(List<MeiliSearchDocument> documents) {
        if (documents == null || documents.isEmpty()) return;

        Map<String, List<MeiliSearchDocument>> groupedByIndex = documents.stream()
                .collect(Collectors.groupingBy(MeiliSearchDocument::getIndexName));

        groupedByIndex.forEach((indexName, docs) ->{
            try {
                String documentsJson = objectMapper.writeValueAsString(docs);
                Index index = meiliSearchClient.index(indexName);
                log.info("MeiliSearchService][addOrUpdateDocument]indexName: {}, content: {}", indexName, documentsJson);
                index.addDocuments(documentsJson, "id");
            } catch (MeilisearchException e) {
                throw new RuntimeException("[" + indexName + "] 인덱스 문서 추가/갱신 실패", e);
            } catch (Exception e) {
                throw new RuntimeException("[" + indexName + "] Json 직렬화 실패", e);
            }
        });
    }

    /**
     * Multi-Search 기반 MeiliSearch 검색.
     * MeiliSearch는 기본적으로 하나의 index에 대한 검색만 수행하지만, 한 번의 쿼리로 여러 index애 대한 검색이 가능하도록
     * multi-search 기능을 지원한다. 이를 통해 여러 협업 툴 각각의 index에 대해 검색을 수행할 수 있다.
     *
     * @param query 사용자가 입력한 검색 쿼리
     * @param indices 검색 대상 indexName을 담은 리스트
     *
     * @return MultiSearchResult을 MeiliSearchQueryResponse로 변환한 객체를 반환한다
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
     * Jira API 응답을 MeiliSearch Document로 변환
     * @param response IssueMetaDataResponse
     * @return MeiliSearchDocument 구현체를 담은 리스트
     */
    public List<MeiliSearchDocument> createDocuments (IssueMetaDataResponse response) {
        // TODO: API 응답 출처에 따라 유연하게 문서를 생성하도록 수정. 현재는 Jira의 IssueMetaDataResponse만 지원.
        return response.issues().stream()
                .map(JiraIssueDocument::from)
                .collect(Collectors.toList());
    }

}
