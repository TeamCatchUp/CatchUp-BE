package com.team.catchup.github.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubApiService {

    @Qualifier("githubWebClient")
    private final WebClient githubWebClient;

    private static final int PER_PAGE = 100;
    private static final Duration RATE_LIMIT_DELAY = Duration.ofMillis(1000); // GitHub API rate limit: 5000 requests/hour (increased for stability)

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
    public Flux<JsonNode> getCommits(String owner, String repo, String since) {
        log.info("[GITHUB][API] Fetching commits for {}/{}", owner, repo);

        return fetchPaginatedData(
                "/repos/" + owner + "/" + repo + "/commits",
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
    public Flux<JsonNode> getPullRequests(String owner, String repo, String state, String since) {
        log.info("[GITHUB][API] Fetching pull requests for {}/{} with state: {}", owner, repo, state);

        String baseUri = "/repos/" + owner + "/" + repo + "/pulls";

        return fetchPaginatedDataWithState(baseUri, state, since);
    }

    /**
     * 특정 Pull Request 상세 조회
     */
    public Mono<JsonNode> getPullRequest(String owner, String repo, int number) {
        String url = String.format("/repos/%s/%s/pulls/%d", owner, repo, number);
        log.info("[GITHUB][API] Fetching pull request: #{}", number);

        return githubWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .delayElement(RATE_LIMIT_DELAY);
    }

    /**
     * Pull Request의 파일 변경 사항 조회
     */
    public Flux<JsonNode> getPullRequestFiles(String owner, String repo, int number) {
        String url = String.format("/repos/%s/%s/pulls/%d/files", owner, repo, number);
        log.info("[GITHUB][API] Fetching PR files: #{}", number);

        return fetchPaginatedDataSimple(url);
    }

    /**
     * Issues 조회 (페이지네이션)
     */
    public Flux<JsonNode> getIssues(String owner, String repo, String state, String since) {
        log.info("[GITHUB][API] Fetching issues for {}/{} with state: {}", owner, repo, state);

        String baseUri = "/repos/" + owner + "/" + repo + "/issues";

        return fetchPaginatedDataWithState(baseUri, state, since);
    }

    /**
     * Issue Comments 조회
     */
    public Flux<JsonNode> getIssueComments(String owner, String repo, int issueNumber) {
        String url = String.format("/repos/%s/%s/issues/%d/comments", owner, repo, issueNumber);
        log.info("[GITHUB][API] Fetching issue comments: #{}", issueNumber);

        return fetchPaginatedDataSimple(url);
    }

    /**
     * Pull Request Reviews 조회
     */
    public Flux<JsonNode> getPullRequestReviews(String owner, String repo, int number) {
        String url = String.format("/repos/%s/%s/pulls/%d/reviews", owner, repo, number);
        log.info("[GITHUB][API] Fetching PR reviews: #{}", number);

        return fetchPaginatedDataSimple(url);
    }

    /**
     * Pull Request Review Comments 조회
     */
    public Flux<JsonNode> getPullRequestReviewComments(String owner, String repo, int number) {
        String url = String.format("/repos/%s/%s/pulls/%d/comments", owner, repo, number);
        log.info("[GITHUB][API] Fetching PR review comments: #{}", number);

        return fetchPaginatedDataSimple(url);
    }

    /**
     * Commit Comments 조회
     */
    public Flux<JsonNode> getCommitComments(String owner, String repo, String sha) {
        String url = String.format("/repos/%s/%s/commits/%s/comments", owner, repo, sha);
        log.info("[GITHUB][API] Fetching commit comments: {}", sha);

        return fetchPaginatedDataSimple(url);
    }

    /**
     * 페이지네이션 데이터 조회 (state, since 파라미터 포함)
     */
    private Flux<JsonNode> fetchPaginatedDataWithState(String baseUri, String state, String since) {
        return Flux.range(1, 1000) // 최대 1000페이지 (100,000개 항목)
                .concatMap(page -> {
                    String uri = baseUri + "?state=" + state + "&per_page=" + PER_PAGE + "&page=" + page;
                    if (since != null && !since.isEmpty()) {
                        uri += "&since=" + since;
                    }

                    return githubWebClient.get()
                            .uri(uri)
                            .retrieve()
                            .bodyToFlux(JsonNode.class)
                            .collectList()
                            .flatMapMany(items -> {
                                if (items.isEmpty()) {
                                    log.info("[GITHUB][API] No more items at page {}, stopping pagination", page);
                                    return Flux.error(new StopPaginationException());
                                }
                                log.info("[GITHUB][API] Fetched page {}: {} items", page, items.size());
                                return Mono.delay(RATE_LIMIT_DELAY)
                                        .thenMany(Flux.fromIterable(items));
                            });
                })
                .onErrorResume(e -> {
                    if (e instanceof StopPaginationException) {
                        log.info("[GITHUB][API] Pagination completed");
                        return Flux.empty();
                    }
                    log.error("[GITHUB][API] Error during pagination", e);
                    return Flux.empty();
                });
    }

    private static class StopPaginationException extends RuntimeException {
    }

    /**
     * 페이지네이션 데이터 조회 (since 파라미터 포함)
     */
    private Flux<JsonNode> fetchPaginatedData(String baseUri, String since) {
        return Flux.range(1, 1000) // 최대 1000페이지
                .concatMap(page -> {
                    String uri = baseUri + "?per_page=" + PER_PAGE + "&page=" + page;
                    if (since != null && !since.isEmpty()) {
                        uri += "&since=" + since;
                    }

                    return githubWebClient.get()
                            .uri(uri)
                            .retrieve()
                            .bodyToFlux(JsonNode.class)
                            .collectList()
                            .flatMapMany(items -> {
                                if (items.isEmpty()) {
                                    log.info("[GITHUB][API] No more items at page {}, stopping pagination", page);
                                    return Flux.error(new StopPaginationException());
                                }
                                log.info("[GITHUB][API] Fetched page {}: {} items", page, items.size());
                                return Mono.delay(RATE_LIMIT_DELAY)
                                        .thenMany(Flux.fromIterable(items));
                            });
                })
                .onErrorResume(e -> {
                    if (e instanceof StopPaginationException) {
                        log.info("[GITHUB][API] Pagination completed");
                        return Flux.empty();
                    }
                    log.error("[GITHUB][API] Error during pagination", e);
                    return Flux.empty();
                });
    }

    /**
     * 단순 페이지네이션 데이터 조회
     */
    private Flux<JsonNode> fetchPaginatedDataSimple(String baseUri) {
        return Flux.range(1, 1000) // 최대 1000페이지
                .concatMap(page -> {
                    String uri = baseUri + "?per_page=" + PER_PAGE + "&page=" + page;

                    return githubWebClient.get()
                            .uri(uri)
                            .retrieve()
                            .bodyToFlux(JsonNode.class)
                            .collectList()
                            .flatMapMany(items -> {
                                if (items.isEmpty()) {
                                    log.info("[GITHUB][API] No more items at page {}, stopping pagination", page);
                                    return Flux.error(new StopPaginationException());
                                }
                                log.info("[GITHUB][API] Fetched page {}: {} items", page, items.size());
                                return Mono.delay(RATE_LIMIT_DELAY)
                                        .thenMany(Flux.fromIterable(items));
                            });
                })
                .onErrorResume(e -> {
                    if (e instanceof StopPaginationException) {
                        log.info("[GITHUB][API] Pagination completed");
                        return Flux.empty();
                    }
                    log.error("[GITHUB][API] Error during pagination", e);
                    return Flux.empty();
                });
    }
}
