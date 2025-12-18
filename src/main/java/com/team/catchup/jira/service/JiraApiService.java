package com.team.catchup.jira.service;

import com.team.catchup.jira.dto.external.IssueMetadataApiResponse;
import com.team.catchup.jira.dto.external.IssueTypeApiResponse;
import com.team.catchup.jira.dto.external.JiraProjectApiResponse;
import com.team.catchup.jira.dto.external.JiraUserApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class JiraApiService {

    private final WebClient jiraWebClient;

    public JiraApiService(@Qualifier("jiraWebClient") WebClient jiraWebClient) {
        this.jiraWebClient = jiraWebClient;
    }

    // TODO : Meilisearch Document 생성 시 필요한 요소들 확정되면 JQL 쿼리 수정
    public Mono<IssueMetadataApiResponse> fetchIssues(
            String projectKey,
            String nextPageToken,
            Integer maxResults,
            boolean fetchAllFields
    ) {
        String jql = "project = " + projectKey;

        log.debug("[JIRA API] Fetching issues - projectKey: {}, nextPageToken: {}",
                projectKey, nextPageToken != null ? "EXISTS" : "null");

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
                .bodyToMono(IssueMetadataApiResponse.class)
                .doOnSuccess(response -> {
                    if (response != null && response.issues() != null) {
                        log.info("[JIRA API] Fetched Issues | PROJECT KEY: {} | ISSUE COUNT: {} | IS_LAST: {}",
                                projectKey, response.issues().size(), response.isLast());
                    }
                    else {
                        log.warn("[JIRA API] Empty Reponse for Project Key : {}", projectKey);
                    }
                })
                .doOnError(error -> log.error("[JIRA API] Failed To Fetch Issues | [PROJECT KEY]: {}", projectKey, error));
    }

    // Pagination, startAt 지원 안됨
    public Mono<List<IssueTypeApiResponse>> fetchIssueTypes() {
        log.debug("[JIRA API] Fetching issue types");

        return jiraWebClient.get()
                .uri("/rest/api/3/issuetype")
                .retrieve()
                .bodyToFlux(IssueTypeApiResponse.class)
                .collectList()
                .doOnSuccess(issueTypes ->
                        log.info("[JIRA API] Issue types fetched - count: {}", issueTypes.size()))
                .doOnError(error ->
                        log.error("[JIRA API] Failed to fetch issue types", error));
    }

    public Mono<List<JiraUserApiResponse>> fetchUsers(Integer startAt, Integer maxResults) {
        log.debug("[JIRA API] Fetching users - startAt: {}, maxResults: {}", startAt, maxResults);

        return jiraWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/3/users/search")
                        .queryParam("startAt", startAt != null ? startAt : 0)
                        .queryParam("maxResults", maxResults != null ? maxResults : 50)
                        .build())
                .retrieve()
                .bodyToFlux(JiraUserApiResponse.class)
                .collectList()
                .doOnSuccess(users ->
                        log.info("[JIRA API] Users fetched - count: {}",
                                users != null ? users.size() : 0))
                .doOnError(error ->
                        log.error("[JIRA API] Failed to fetch users", error));
    }

    public Mono<JiraProjectApiResponse> fetchProjects(Integer startAt, Integer maxResults) {
        log.debug("[JIRA API] Fetching projects - startAt: {}, maxResults: {}", startAt, maxResults);

        return jiraWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/3/project/search")
                        .queryParam("expand", "description,insight")
                        .queryParam("startAt", startAt != null ? startAt : 0)
                        .queryParam("maxResults", maxResults != null ? maxResults : 100)
                        .build())
                .retrieve()
                .bodyToMono(JiraProjectApiResponse.class)
                .doOnSuccess(response ->
                        log.info("[JIRA API] Projects fetched - count: {}, total: {}",
                                response != null && response.values() != null ? response.values().size() : 0,
                                response != null ? response.total() : 0))
                .doOnError(error ->
                        log.error("[JIRA API] Failed to fetch projects", error));
    }
}
