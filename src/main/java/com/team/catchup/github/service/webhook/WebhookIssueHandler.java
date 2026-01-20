package com.team.catchup.github.service.webhook;

import com.team.catchup.github.dto.webhook.WebhookPrIssuePayload;
import com.team.catchup.github.entity.GithubIssue;
import com.team.catchup.github.entity.GithubRepository;
import com.team.catchup.github.repository.GithubIssueRepository;
import com.team.catchup.github.repository.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookIssueHandler {

    private final GithubRepositoryRepository repositoryRepository;
    private final GithubIssueRepository issueRepository;

    public void handleIssueEvent(WebhookPrIssuePayload payload) {
        String owner = payload.getRepository().getOwner().getLogin();
        String repo = payload.getRepository().getName();

        WebhookPrIssuePayload.Issue issue = payload.getIssue();
        Integer issueNumber = issue.getNumber();
        String action = payload.getAction();

        GithubRepository repository = repositoryRepository.findByOwnerAndName(owner, repo)
                .orElseThrow(() -> {
                    log.error("[Webhook][Issue] Repository {}/{} not found. Full sync required.", owner, repo);
                    return new IllegalStateException("Repository not synced: " + owner + "/" + repo);
                });

        log.info("[Webhook][Issue] Processing Issue #{} for {}/{} - action: {}",
                issueNumber, owner, repo, action);

        // Issue 메타데이터 저장/업데이트
        issueRepository.findById(issue.getId()).ifPresentOrElse(
                existingIssue -> {
                    GithubIssue.IssueStatus status = parseIssueStatus(issue.getState());
                    LocalDateTime updatedAt = parseDateTime(issue.getUpdatedAt());
                    LocalDateTime closedAt = parseDateTime(issue.getClosedAt());

                    existingIssue.updateFromWebhook(issue.getTitle(), status, updatedAt, closedAt);
                    issueRepository.save(existingIssue);

                    log.info("[Webhook][Issue] Updated Issue #{} for {}/{}", issueNumber, owner, repo);
                },
                () -> {
                    GithubIssue newIssue = GithubIssue.builder()
                            .issueId(issue.getId())
                            .repository(repository)
                            .number(issue.getNumber())
                            .title(issue.getTitle())
                            .status(parseIssueStatus(issue.getState()))
                            .authorLogin(issue.getUser() != null ? issue.getUser().getLogin() : null)
                            .createdAt(parseDateTime(issue.getCreatedAt()))
                            .updatedAt(parseDateTime(issue.getUpdatedAt()))
                            .closedAt(parseDateTime(issue.getClosedAt()))
                            .htmlUrl(issue.getHtmlUrl())
                            .build();

                    issueRepository.save(newIssue);

                    log.info("[Webhook][Issue] Created new Issue #{} for {}/{}", issueNumber, owner, repo);
                }
        );
    }

    private GithubIssue.IssueStatus parseIssueStatus(String state) {
        if (state == null) {
            return GithubIssue.IssueStatus.OPEN;
        }
        return "closed".equalsIgnoreCase(state) ? GithubIssue.IssueStatus.CLOSED : GithubIssue.IssueStatus.OPEN;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(dateTimeStr).toLocalDateTime();
        } catch (Exception e) {
            log.warn("[Webhook][Issue] Failed to parse datetime: {}", dateTimeStr);
            return null;
        }
    }
}
