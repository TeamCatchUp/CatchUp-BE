package com.team.catchup.notion.service;

import com.team.catchup.notion.dto.external.NotionSearchApiResponse;
import com.team.catchup.notion.dto.external.NotionUserApiResponse;
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
    public Mono<List<NotionSearchApiResponse.NotionPageResult>> fetchAllPages() {
        log.info("[NOTION] Page Search Started");
        return fetchPagesRecursively(null, new ArrayList<>());
    }

    public Mono<List<NotionUserApiResponse.NotionUserResult>> fetchAllUsers() {
        log.info("[NOTION] User Search Started");
        return fetchUsersRecursively(null, new ArrayList<>());
    }

    private Mono<List<NotionSearchApiResponse.NotionPageResult>> fetchPagesRecursively(
            String startCursor, List<NotionSearchApiResponse.NotionPageResult> accumulatedPages
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
                        log.debug("[NOTION] Next Cursor Exists -> Requesting Next Page Batch");
                        return fetchPagesRecursively(response.nextCursor(), accumulatedPages);
                    } else {
                        log.info("[NOTION] Page Search Completed | Total Page: {}", accumulatedPages.size());
                        return Mono.just(accumulatedPages);
                    }
                });
    }

    private Mono<List<NotionUserApiResponse.NotionUserResult>> fetchUsersRecursively(
            String startCursor, List<NotionUserApiResponse.NotionUserResult> accumulatedUsers) {

        return Mono.delay(Duration.ofMillis(340))
                .then(fetchUserBatch(startCursor))
                .flatMap(response -> {
                    if (response.results() != null) {
                        List<NotionUserApiResponse.NotionUserResult> personUsers = response.results().stream()
                                .filter(user -> "person".equals(user.type()))
                                .toList();
                        accumulatedUsers.addAll(personUsers);
                    }

                    log.info("[Notion Batch] 가져온 개수: {}, hasMore: {}, nextCursor: {}",
                            response.results() != null ? response.results().size() : 0,
                            response.hasMore(),
                            response.nextCursor());

                    if(response.hasMore() && response.nextCursor() != null) {
                        log.debug("[NOTION] Next Cursor Exists -> Requesting Next User Batch");
                        return fetchUsersRecursively(response.nextCursor(), accumulatedUsers);
                    } else {
                        log.info("[NOTION] User Search Completed | Total Page: {}", accumulatedUsers.size());
                        return Mono.just(accumulatedUsers);
                    }
                });
    }

    private Mono<NotionSearchApiResponse> fetchPageBatch(String startCursor) {
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
                .bodyToMono(NotionSearchApiResponse.class)
                .doOnError(error -> log.error("[NOTION] Page Full Sync Failed", error));
    }

    private Mono<NotionUserApiResponse> fetchUserBatch(String startCursor) {
        return notionWebClient.get()
                .uri(uriBuilder ->{
                    var builder = uriBuilder.path("v1/users");
                    if(startCursor != null) {
                        builder.queryParam("start_cursor", startCursor);
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(NotionUserApiResponse.class)
                .doOnError(error -> log.error("[NOTION] Page Fetch Failed", error));
    }
}
