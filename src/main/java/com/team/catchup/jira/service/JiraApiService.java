package com.team.catchup.jira.service;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class JiraApiService {

    private final WebClient jiraWebClient;

    public Mono<IssueMetaDataResponse> fetchIssuesWithToken(String projectKey, String nextPageToken, Integer maxResults, boolean fetchAllFields) {
        String jql = "project = " + projectKey;

        log.info("=== API 호출 시작 ===");
        log.info("JQL: {}, NextPageToken: {}", jql, nextPageToken != null ? "EXISTS" : "null");

        return jiraWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/rest/api/3/search/jql")
                            .queryParam("jql", jql)
                            .queryParam("maxResults", maxResults);

                    // 이슈 ID만 필요한 경우에는 이 필드를 제외해서 가벼운 응답 받을 있도록 !
                    if (fetchAllFields) {
                        builder.queryParam("fields", "*all");
                    }

                    // 마지막 페이지가 아닌 경우 다음 페이지의 토큰 추가
                    if (nextPageToken != null && !nextPageToken.isBlank()) {
                        builder.queryParam("nextPageToken", nextPageToken);
                    }

                    return builder.build();
                })
                .retrieve()
                .bodyToMono(IssueMetaDataResponse.class)
                .doOnSuccess(response -> {
                    if (response != null && response.issues() != null) {
                        log.info("[JIRA] 동기화 성공 | [PROJECT KEY]: {} | [ISSUE COUNT]: {} | [IS_LAST]: {}",
                                projectKey, response.issues().size(), response.isLast());
                    }
                })
                .doOnError(error -> log.error("[JIRA] 동기화 실패 | [PROJECT KEY]: {}", projectKey, error));
    }
}
