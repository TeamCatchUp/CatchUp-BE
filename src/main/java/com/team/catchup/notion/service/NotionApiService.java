package com.team.catchup.notion.service;

import com.team.catchup.notion.dto.NotionSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class NotionApiService {
    
    private final WebClient notionWebClient;
    
    public NotionApiService(@Qualifier("notionWebClient") WebClient notionWebClient) {
        this.notionWebClient = notionWebClient;
    }

    // API Key에 접근 권한이 부여된 모든 페이지 조회
    // 최상위 페이지 + 최상위 페이지 아래에서 선언된 페이지 + 데이터베이스에 행으로 들어가있는 페이지
    public Mono<List<NotionSearchResponse.NotionPageResult>> fetchAllPages() {
        log.info("[NOTION] Page Search Started");
        return fetchPagesRecursively(null, new ArrayList<>());
    }

    public Mono<List<NotionSearchResponse.NotionPageResult>> fetchPagesRecursively(
            String startCursor, List<NotionSearchResponse.NotionPageResult> accumulatedPages
    ) {
        // Notion API 호출 제한 = 1초당 3회 -> 0.34초에 1회 호출하도록 딜레이 걸어주기
        return Mono.delay(Duration.ofMillis(340))
                .then(fetchPageBatch(startCursor))
                .flatMap(response -> {
                    if (response.results() != null) {
                        accumulatedPages.addAll(response.results());
                    }

                    log.info("[Notion Batch] 가져온 개수: {}, hasMore: {}, nextCursor: {}",
                            response.results() != null ? response.results().size() : 0,
                            response.hasMore(),
                            response.nextCursor());

                    if (response.hasMore() && response.nextCursor() != null) {
                        log.debug("[NOTION] Next Cursor Exists -> Requesting Next Page");
                        return fetchPagesRecursively(response.nextCursor(), accumulatedPages);
                    } else {
                        log.info("[NOTION] Page Search Completed | Total Page: {}", accumulatedPages.size());
                        return Mono.just(accumulatedPages);
                    }
                });
    }

    private Mono<NotionSearchResponse> fetchPageBatch(String startCursor) {
        Map<String, Object> body = new HashMap<>();

        body.put("filter", Map.of(
                "value", "page",
                "property", "object"
        ));

        body.put("sort", Map.of(
                "direction", "descending",
                "timestamp", "last_edited_time"
        ));

        if(startCursor != null) {
            body.put("start_cursor", startCursor);
        }

        return notionWebClient.post()
                .uri("/v1/search")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(NotionSearchResponse.class)
                .doOnError(error -> log.error("[NOTION] Page Full Sync Failed", error));
    }
}
