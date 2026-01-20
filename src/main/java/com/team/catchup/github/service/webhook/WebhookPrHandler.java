package com.team.catchup.github.service.webhook;

import com.team.catchup.github.dto.webhook.WebhookPrIssuePayload;
import com.team.catchup.github.entity.GithubPullRequest;
import com.team.catchup.github.entity.GithubRepository;
import com.team.catchup.github.repository.GithubPullRequestRepository;
import com.team.catchup.github.repository.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * Pull Request Webhook 이벤트 처리 핸들러
 * PR 메타데이터를 DB에 저장/업데이트
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookPrHandler {

    private final GithubRepositoryRepository repositoryRepository;
    private final GithubPullRequestRepository pullRequestRepository;

    public void handlePrEvent(WebhookPrIssuePayload payload) {
        String owner = payload.getRepository().getOwner().getLogin();
        String repo = payload.getRepository().getName();

        WebhookPrIssuePayload.PullRequest pr = payload.getPullRequest();
        Integer prNumber = pr.getNumber();
        String action = payload.getAction();

        GithubRepository repository = repositoryRepository.findByOwnerAndName(owner, repo)
                .orElseThrow(() -> {
                    log.error("[Webhook][PR] Repository {}/{} not found. Full sync required.", owner, repo);
                    return new IllegalStateException("Repository not synced: " + owner + "/" + repo);
                });

        log.info("[Webhook][PR] Processing PR #{} for {}/{} - action: {}",
                prNumber, owner, repo, action);

        // PR 메타데이터 저장/업데이트
        pullRequestRepository.findById(pr.getId()).ifPresentOrElse(
                existingPr -> {
                    GithubPullRequest.PullRequestStatus status = parsePrStatus(pr.getState(), pr.getMerged());
                    LocalDateTime updatedAt = parseDateTime(pr.getUpdatedAt());
                    LocalDateTime closedAt = parseDateTime(pr.getClosedAt());
                    LocalDateTime mergedAt = parseDateTime(pr.getMergedAt());

                    existingPr.updateFromWebhook(pr.getTitle(), status, updatedAt, closedAt, mergedAt, pr.getMergeCommitSha());
                    pullRequestRepository.save(existingPr);

                    log.info("[Webhook][PR] Updated PR #{} for {}/{}", prNumber, owner, repo);
                },
                () -> {
                    GithubPullRequest newPr = GithubPullRequest.builder()
                            .pullRequestId(pr.getId())
                            .repository(repository)
                            .number(pr.getNumber())
                            .title(pr.getTitle())
                            .status(parsePrStatus(pr.getState(), pr.getMerged()))
                            .authorLogin(pr.getUser() != null ? pr.getUser().getLogin() : null)
                            .baseBranch(pr.getBase() != null ? pr.getBase().getRef() : null)
                            .headBranch(pr.getHead() != null ? pr.getHead().getRef() : null)
                            .mergeCommitSha(pr.getMergeCommitSha())
                            .createdAt(parseDateTime(pr.getCreatedAt()))
                            .updatedAt(parseDateTime(pr.getUpdatedAt()))
                            .closedAt(parseDateTime(pr.getClosedAt()))
                            .mergedAt(parseDateTime(pr.getMergedAt()))
                            .htmlUrl(pr.getHtmlUrl())
                            .build();

                    pullRequestRepository.save(newPr);

                    log.info("[Webhook][PR] Created new PR #{} for {}/{}", prNumber, owner, repo);
                }
        );
    }

    private GithubPullRequest.PullRequestStatus parsePrStatus(String state, Boolean merged) {
        if (merged != null && merged) {
            return GithubPullRequest.PullRequestStatus.MERGED;
        }
        if (state == null) {
            return GithubPullRequest.PullRequestStatus.OPEN;
        }
        return "closed".equalsIgnoreCase(state) ? GithubPullRequest.PullRequestStatus.CLOSED : GithubPullRequest.PullRequestStatus.OPEN;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(dateTimeStr).toLocalDateTime();
        } catch (Exception e) {
            log.warn("[Webhook][PR] Failed to parse datetime: {}", dateTimeStr);
            return null;
        }
    }
}
