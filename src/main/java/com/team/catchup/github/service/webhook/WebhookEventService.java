package com.team.catchup.github.service.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.catchup.common.config.RabbitConfig;
import com.team.catchup.github.dto.webhook.WebhookPrIssuePayload;
import com.team.catchup.github.dto.webhook.WebhookPushPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookEventService {

    private final ObjectMapper objectMapper;
    private final WebhookPushHandler pushHandler;
    private final WebhookPrHandler prHandler;
    private final WebhookIssueHandler issueHandler;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void handlePushEvent(String payload) throws JsonProcessingException {
        WebhookPushPayload pushPayload = objectMapper.readValue(payload, WebhookPushPayload.class);

        String owner = pushPayload.getRepository().getOwner().getLogin();
        String repo = pushPayload.getRepository().getName();
        Long repositoryId = pushPayload.getRepository().getId();
        String branchName = pushPayload.getBranchName();

        log.info("[Webhook][Push] Processing push event for {}/{} (branch: {}) - {} commits",
                owner, repo, branchName, pushPayload.getCommits().size());

        // Commit 메타데이터 저장 및 변경된 파일 경로 수집
        List<String> changedFilePaths = pushHandler.handlePushEvent(pushPayload);

        // targetBranch가 아니면 빈 리스트 반환되어 이벤트 발행하지 않음
        if (changedFilePaths.isEmpty()) {
            log.info("[Webhook][Push] No changes to publish for {}/{}", owner, repo);
            return;
        }

        // Worker에게 Event Publishing
        publishPushWorkerEvent(repositoryId, owner, repo, pushPayload.getRef(), branchName, changedFilePaths);

        log.info("[Webhook][Push] Completed push event processing for {}/{} (branch: {}) - {} files changed",
                owner, repo, branchName, changedFilePaths.size());
    }

    @Transactional
    public void handlePrEvent(String payload) throws JsonProcessingException {
        WebhookPrIssuePayload prPayload = objectMapper.readValue(payload, WebhookPrIssuePayload.class);

        String owner = prPayload.getRepository().getOwner().getLogin();
        String repo = prPayload.getRepository().getName();
        Integer prNumber = prPayload.getPullRequest().getNumber();
        String action = prPayload.getAction();

        log.info("[Webhook][PR] Processing PR event - action: {}, PR #{} in {}/{}",
                action, prNumber, owner, repo);

        // PR 메타데이터 저장/업데이트
        prHandler.handlePrEvent(prPayload);

        // Worker에게 Event Publishing
        publishPrWorkerEvent(prPayload.getRepository().getId(), owner, repo, prNumber, action);

        log.info("[Webhook][PR] Completed PR event processing - PR #{} in {}/{}",
                prNumber, owner, repo);
    }

    @Transactional
    public void handleIssueEvent(String payload) throws JsonProcessingException {
        WebhookPrIssuePayload issuePayload = objectMapper.readValue(payload, WebhookPrIssuePayload.class);

        String owner = issuePayload.getRepository().getOwner().getLogin();
        String repo = issuePayload.getRepository().getName();
        Integer issueNumber = issuePayload.getIssue().getNumber();
        String action = issuePayload.getAction();

        log.info("[Webhook][Issue] Processing issue event - action: {}, Issue #{} in {}/{}",
                action, issueNumber, owner, repo);

        // Issue 메타데이터 저장/업데이트
        issueHandler.handleIssueEvent(issuePayload);

        // Worker에게 Event Publishing
        publishIssueWorkerEvent(issuePayload.getRepository().getId(), owner, repo, issueNumber, action);

        log.info("[Webhook][Issue] Completed issue event processing - Issue #{} in {}/{}",
                issueNumber, owner, repo);
    }

    /**
     * Push 이벤트를 Worker에게 전달
     */
    private void publishPushWorkerEvent(Long repositoryId, String owner, String repo,
                                        String ref, String branch, List<String> changedFilePaths) {
        WorkerEventMessage message = WorkerEventMessage.builder()
                .eventType(WorkerEventMessage.EventType.PUSH)
                .repositoryId(repositoryId)
                .owner(owner)
                .repo(repo)
                .ref(ref)
                .branch(branch)
                .changedFilePaths(changedFilePaths)
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.GITHUB_REPOSITORY_QUEUE, message);
        log.info("[Webhook] Published PUSH event to worker for {}/{} (branch: {}) - {} files changed",
                owner, repo, branch, changedFilePaths.size());
    }

    /**
     * Pull Request 이벤트를 Worker에게 전달
     */
    private void publishPrWorkerEvent(Long repositoryId, String owner, String repo,
                                      Integer number, String action) {
        WorkerEventMessage message = WorkerEventMessage.builder()
                .eventType(WorkerEventMessage.EventType.PULL_REQUEST)
                .repositoryId(repositoryId)
                .owner(owner)
                .repo(repo)
                .number(number)
                .action(action)
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.GITHUB_PULL_REQUEST_QUEUE, message);
        log.info("[Webhook] Published PULL_REQUEST event to worker for {}/{} PR #{} - action: {}",
                owner, repo, number, action);
    }

    /**
     * Issue 이벤트를 Worker에게 전달
     */
    private void publishIssueWorkerEvent(Long repositoryId, String owner, String repo,
                                         Integer number, String action) {
        WorkerEventMessage message = WorkerEventMessage.builder()
                .eventType(WorkerEventMessage.EventType.ISSUE)
                .repositoryId(repositoryId)
                .owner(owner)
                .repo(repo)
                .number(number)
                .action(action)
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.GITHUB_ISSUE_QUEUE, message);
        log.info("[Webhook] Published ISSUE event to worker for {}/{} Issue #{} - action: {}",
                owner, repo, number, action);
    }
}
