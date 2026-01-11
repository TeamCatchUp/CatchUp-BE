package com.team.catchup.github.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class GithubApiService {

    private final WebClient githubWebClient;

    private static final int PER_PAGE = 100;
    private static final Duration RATE_LIMIT_DELAY = Duration.ofMillis(1000); // GitHub API rate limit: 5000 requests/hour

    public GithubApiService(@Qualifier("githubWebClient") WebClient githubWebClient) {
        this.githubWebClient = githubWebClient;
    }

    /**
     * Repository 정보 조회
     */
    public Mono<JsonNode> getRepository(String owner, String repo) {
        String url = String.format("/repos/%s/%s", owner, repo);
        log.info("[GITHUB][API] Fetching repository: {}/{}", owner, repo);

        return githubWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnError(e -> log.error("[GITHUB][API] Failed to fetch repository: {}/{}", owner, repo, e));
    }

    /**
     * Commits 조회 (페이지네이션)
     * since 파라미터는 이후 증분 동기화에 사용할 예정 -> 특정 시점 이후의 커밋만 가져오기
     */
    public Flux<JsonNode> getCommits(String owner, String repo, String branch, String since) {
        log.info("[GITHUB][API] Fetching commits for {}/{} on branch: {}", owner, repo, branch);

        return fetchPaginatedCommits(
                "/repos/" + owner + "/" + repo + "/commits",
                branch,
                since
        );
    }

    /**
     * 특정 Commit 상세 정보 조회 (파일 변경 정보 포함)
     */
    public Mono<JsonNode> getCommit(String owner, String repo, String sha) {
        String url = String.format("/repos/%s/%s/commits/%s", owner, repo, sha);
        log.info("[GITHUB][API] Fetching commit details: {}", sha);

        return githubWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .delayElement(RATE_LIMIT_DELAY);
    }

    /**
     * Pull Requests 조회 (페이지네이션)
     */
    public Flux<JsonNode> getPullRequests(String owner, String repo, String base, String state, String since) {
        log.info("[GITHUB][API] Fetching pull requests for {}/{} with base: {}, state: {}",
                owner, repo, base, state);

        return fetchPaginatedPullRequests(
                "/repos/" + owner + "/" + repo + "/pulls",
                base,
                state,
                since
        );
    }

    /**
     * Pull Request의 파일 변경 사항 조회
     */
    public Flux<JsonNode> getPullRequestFiles(String owner, String repo, int number) {
        String url = String.format("/repos/%s/%s/pulls/%d/files", owner, repo, number);
        log.info("[GITHUB][API] Fetching PR files: #{}", number);

        return fetchPaginatedData(url);
    }

    /**
     * Issues 조회 (페이지네이션)
     */
    public Flux<JsonNode> getIssues(String owner, String repo, String state, String since) {
        log.info("[GITHUB][API] Fetching issues for {}/{} with state: {}", owner, repo, state);

        return fetchPaginatedIssues(
                "/repos/" + owner + "/" + repo + "/issues",
                state,
                since
        );
    }

    /**
     * Issue Comments 조회
     */
    public Flux<JsonNode> getIssueComments(String owner, String repo, int issueNumber) {
        String url = String.format("/repos/%s/%s/issues/%d/comments", owner, repo, issueNumber);
        log.info("[GITHUB][API] Fetching issue comments: #{}", issueNumber);

        return fetchPaginatedData(url);
    }

    /**
     * Pull Request Reviews 조회
     */
    public Flux<JsonNode> getPullRequestReviews(String owner, String repo, int number) {
        String url = String.format("/repos/%s/%s/pulls/%d/reviews", owner, repo, number);
        log.info("[GITHUB][API] Fetching PR reviews: #{}", number);

        return fetchPaginatedData(url);
    }

    /**
     * Pull Request Review Comments 조회
     */
    public Flux<JsonNode> getPullRequestReviewComments(String owner, String repo, int number) {
        String url = String.format("/repos/%s/%s/pulls/%d/comments", owner, repo, number);
        log.info("[GITHUB][API] Fetching PR review comments: #{}", number);

        return fetchPaginatedData(url);
    }

    // ==================== Private Pagination Methods ====================

    /**
     * Commits용 페이지네이션 (sha 파라미터)
     */
    private Flux<JsonNode> fetchPaginatedCommits(String baseUri, String branch, String since) {
        return executePagination(uriBuilder -> {
            String uri = uriBuilder
                    .append(baseUri)
                    .append("?sha=").append(branch)
                    .append("&per_page=").append(PER_PAGE)
                    .toString();

            if (since != null && !since.isEmpty()) {
                uri += "&since=" + since;
            }

            return uri;
        });
    }

    /**
     * Pull Requests용 페이지네이션 (base, state 파라미터)
     */
    private Flux<JsonNode> fetchPaginatedPullRequests(String baseUri, String base, String state, String since) {
        return executePagination(uriBuilder -> {
            String uri = uriBuilder
                    .append(baseUri)
                    .append("?base=").append(base)
                    .append("&state=").append(state)
                    .append("&per_page=").append(PER_PAGE)
                    .toString();

            if (since != null && !since.isEmpty()) {
                uri += "&since=" + since;
            }

            return uri;
        });
    }

    /**
     * Issues용 페이지네이션 (state 파라미터)
     */
    private Flux<JsonNode> fetchPaginatedIssues(String baseUri, String state, String since) {
        return executePagination(uriBuilder -> {
            String uri = uriBuilder
                    .append(baseUri)
                    .append("?state=").append(state)
                    .append("&per_page=").append(PER_PAGE)
                    .toString();

            if (since != null && !since.isEmpty()) {
                uri += "&since=" + since;
            }

            return uri;
        });
    }

    /**
     * 기타(Comments, Reviews, Files)용 페이지네이션
     */
    private Flux<JsonNode> fetchPaginatedData(String baseUri) {
        return executePagination(uriBuilder ->
                uriBuilder
                        .append(baseUri)
                        .append("?per_page=").append(PER_PAGE)
                        .toString()
        );
    }

    private Flux<JsonNode> executePagination(UriBuilderFunction uriBuilderFunction) {
        return Flux.range(1, 1000)
                .concatMap(page -> {
                    StringBuilder uriBuilder = new StringBuilder();
                    String baseUri = uriBuilderFunction.buildUri(uriBuilder);
                    String uri = baseUri + "&page=" + page;

                    return fetchPage(uri);
                })
                .onErrorResume(this::handlePaginationError);
    }

    private Flux<JsonNode> fetchPage(String uri) {
        return githubWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .collectList()
                .flatMapMany(items -> {
                    if (items.isEmpty()) {
                        log.info("[GITHUB][API] No more items, stopping pagination");
                        return Flux.error(new StopPaginationException());
                    }
                    log.info("[GITHUB][API] Fetched {} items", items.size());
                    return Mono.delay(RATE_LIMIT_DELAY)
                            .thenMany(Flux.fromIterable(items));
                });
    }

    private Flux<JsonNode> handlePaginationError(Throwable e) {
        if (e instanceof StopPaginationException) {
            log.info("[GITHUB][API] Pagination completed");
            return Flux.empty();
        }
        log.error("[GITHUB][API] Error during pagination", e);
        return Flux.empty();
    }

    // ==================== Functional Interface & Exception ====================

    @FunctionalInterface
    private interface UriBuilderFunction {
        String buildUri(StringBuilder builder);
    }

    // 추후 ErrorCode으로 이동
    private static class StopPaginationException extends RuntimeException {
    }
}