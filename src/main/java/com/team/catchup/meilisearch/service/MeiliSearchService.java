package com.team.catchup.meilisearch.service;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.IndexSearchRequest;
import com.meilisearch.sdk.MultiSearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchApiException;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.json.GsonJsonHandler;
import com.meilisearch.sdk.model.*;
import com.team.catchup.jira.entity.IssueMetadata;
import com.team.catchup.jira.repository.IssueMetaDataRepository;
import com.team.catchup.meilisearch.document.JiraIssueDocument;
import com.team.catchup.meilisearch.document.MeiliSearchDocument;
import com.team.catchup.meilisearch.dto.MeiliSearchQueryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
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
    private final IssueMetaDataRepository issueMetaDataRepository;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Transactional(readOnly = true)
    public void syncAllJiraIssues() {
        List<IssueMetadata> allIssues = issueMetaDataRepository.findAll();
        if (allIssues.isEmpty()) return;

        List<MeiliSearchDocument> documents = allIssues.stream()
                .map(entity -> (MeiliSearchDocument) JiraIssueDocument.from(entity))
                .toList();

        log.info("[MeiliSearch] DB 데이터 {}건 동기화 시작", documents.size());
        addOrUpdateDocument(documents);
    }

    /**
     * MeiliSearch Document 생성 또는 갱신.
     * 새로운 Document를 생성하고 이미 존재하는 Document라면 덮어쓴다.
     *
     * @param documents MeiliSearchDocument 구현체의 리스트
     */
    public void addOrUpdateDocument(List<MeiliSearchDocument> documents) {
        if (documents == null || documents.isEmpty()) return;

        Map<String, List<MeiliSearchDocument>> groupedByIndex = documents.stream()
                .collect(Collectors.groupingBy(MeiliSearchDocument::getIndexName));

        groupedByIndex.forEach((indexName, docs) ->{
            try {
                String documentsJson = jsonHandler.encode(docs);
                Index index = meiliSearchClient.index(indexName);

                // Primary Key 추출
                String primaryKey = docs.get(0).getPrimaryKeyFieldName();

                try {
                    // Embedder 설정 (임시)
                    Settings currentSettings = index.getSettings();
                    if (currentSettings.getEmbedders() == null || !currentSettings.getEmbedders().containsKey("default")) {
                        log.info("[Index: {}] Embedder 설정 미적용 -> 설정 진행", indexName); // 추후 Worker로 위임
                        configureEmbedder(index);
                    }
                } catch (MeilisearchApiException e) { // 인덱스 없을 경우
                    if ("index_not_found".equals(e.getCode())) {
                        log.info("[Index: {}] 인덱스 없음 -> 생성 및 설정 (PK: {})", indexName, primaryKey);
                        meiliSearchClient.createIndex(indexName, primaryKey);
                        configureEmbedder(index);
                    } else {
                        throw e;
                    }
                }

                log.info("[MeiliSearchService][addOrUpdateDocument] indexName: {}, docCount: {}", indexName, docs.size());
                index.addDocuments(documentsJson);
            } catch (MeilisearchException e) {
                throw new RuntimeException("[" + indexName + "] 인덱스 문서 추가/갱신 실패", e);
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

    private void configureEmbedder(Index index) {
        try {
            Embedder openAiEmbedder = new Embedder();
            openAiEmbedder.setSource(EmbedderSource.OPEN_AI);
            openAiEmbedder.setModel("text-embedding-3-large");
            openAiEmbedder.setDimensions(3072);
            openAiEmbedder.setApiKey(openAiApiKey);

            HashMap<String, Embedder> embedders = new HashMap<>();
            embedders.put("default", openAiEmbedder);

            Settings settings = new Settings();
            settings.setEmbedders(embedders);

            index.updateSettings(settings);
            log.info("[MeiliSearch] Embedder 설정 완료: {}", index);
        } catch (Exception e) {
            log.error("[MeiliSearch] Embedder 설정 중 오류 발생: {}", e.getMessage());
        }
    }
}