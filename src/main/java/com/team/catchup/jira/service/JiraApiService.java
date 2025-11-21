package com.team.catchup.jira.service;

import com.team.catchup.jira.dto.response.IssueMetaDataResponse;
import com.team.catchup.jira.dto.response.IssueTypeResponse;
import com.team.catchup.jira.dto.response.JiraProjectResponse;
import com.team.catchup.jira.dto.response.JiraUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JiraApiService {

    private final WebClient jiraWebClient;

    // TODO : Meilisearch Document 생성 시 필요한 요소들 확정되면 JQL 쿼리 수정
    public Mono<IssueMetaDataResponse> fetchIssues(String projectKey, String nextPageToken, Integer maxResults, boolean fetchAllFields) {
        String jql = "project = " + projectKey;

        log.info("=== POST Issue MetaData API 호출 시작 ===");
        log.info("JQL: {}, NextPageToken: {}", jql, nextPageToken != null ? "EXISTS" : "null");

        return jiraWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/rest/api/3/search/jql")
                            .queryParam("jql", jql)
                            .queryParam("maxResults", maxResults);

                    // fetchAllFields = true -> queryParameter 에 fields:*all 추가
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
                    else {
                        log.warn("[JIRA] Issue 응답이 비어있습니다");
                    }
                })
                .doOnError(error -> log.error("[JIRA] 동기화 실패 | [PROJECT KEY]: {}", projectKey, error));
    }

    // Pagination, startAt 지원 안됨
    public Mono<List<IssueTypeResponse>> fetchIssueTypes() {
        log.info("=== POST Issue Types API 호출 시작 ===");

        return jiraWebClient.get()
                .uri("/rest/api/3/issuetype")
                .retrieve()
                .bodyToFlux(IssueTypeResponse.class)
                .collectList()
                .doOnSuccess(issueTypes ->
                        log.info("[JIRA] IssueType 조회 성공 | Count: {}", issueTypes.size()))
                .doOnError(error ->
                        log.error("[JIRA] IssueType 조회 실패", error));
    }

    public Mono<List<JiraUserResponse>> fetchUsers(Integer startAt, Integer maxResults) {
        log.info("=== POST Jira Users API 호출 시작 ===");
        log.info("StartAt: {}, MaxResults: {}", startAt, maxResults);

        return jiraWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/3/users/search")
                        .queryParam("startAt", startAt != null ? startAt : 0)
                        .queryParam("maxResults", maxResults != null ? maxResults : 50)
                        .build())
                .retrieve()
                .bodyToFlux(JiraUserResponse.class)
                .collectList()
                .doOnSuccess(users ->
                        log.info("[JIRA] User 조회 성공 | Count: {}",
                                users != null ? users.size() : 0))
                .doOnError(error ->
                        log.error("[JIRA] User 조회 실패", error));
    }

    public Mono<JiraProjectResponse> fetchProjects(Integer startAt, Integer maxResults) {
        log.info("=== POST Projects API 호출 시작 ===");
        log.info("StartAt: {}, MaxResults: {}", startAt, maxResults);

        return jiraWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/3/project/search")
                        .queryParam("expand", "description,insight")
                        .queryParam("startAt", startAt != null ? startAt : 0)
                        .queryParam("maxResults", maxResults != null ? maxResults : 100)
                        .build())
                .retrieve()
                .bodyToMono(JiraProjectResponse.class)
                .doOnSuccess(response ->
                        log.info("[JIRA] Project 조회 성공 | Count: {}, Total: {}",
                                response != null && response.values() != null ? response.values().size() : 0,
                                response != null ? response.total() : 0))
                .doOnError(error ->
                        log.error("[JIRA] Project 조회 실패", error));
    }}
