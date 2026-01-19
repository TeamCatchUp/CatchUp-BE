package com.team.catchup.github.service.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.catchup.common.config.RabbitConfig;
import com.team.catchup.github.dto.webhook.WebhookEventMessage;
import com.team.catchup.github.dto.webhook.WebhookPrIssuePayload;
import com.team.catchup.github.dto.webhook.WebhookPushPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookEventService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void handlePushEvent(String payload) throws JsonProcessingException {
        WebhookPushPayload pushPayload = objectMapper.readValue(payload, WebhookPushPayload.class);

        WebhookEventMessage message = WebhookEventMessage.builder()
                .eventType("push")
                .repositoryId(pushPayload.getRepository().getId())
                .owner(pushPayload.getRepository().getOwner().getLogin())
                .repo(pushPayload.getRepository().getName())
                .ref(pushPayload.getRef())
                .commitShas(pushPayload.getCommits().stream()
                        .map(WebhookPushPayload.Commit::getId)
                        .collect(Collectors.toList()))
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.GITHUB_PUSH_EVENT_QUEUE, message);
        log.info("[Github][Webhook] Sent Push Event to {}", RabbitConfig.GITHUB_PUSH_EVENT_QUEUE);
    }

    public void handlePrIssueEvent(String eventType, String payload) throws JsonProcessingException {
        WebhookPrIssuePayload prIssuePayload = objectMapper.readValue(payload, WebhookPrIssuePayload.class);

        Integer number = (prIssuePayload.getPullRequest() != null) ? prIssuePayload.getPullRequest().getNumber() : prIssuePayload.getIssue().getNumber();
        String title = (prIssuePayload.getPullRequest() != null) ? prIssuePayload.getPullRequest().getTitle() : prIssuePayload.getIssue().getTitle();

        WebhookEventMessage message = WebhookEventMessage.builder()
                .eventType(eventType)
                .action(prIssuePayload.getAction())
                .repositoryId(prIssuePayload.getRepository().getId())
                .owner(prIssuePayload.getRepository().getOwner().getLogin())
                .repo(prIssuePayload.getRepository().getName())
                .issueNumber(number)
                .title(title)
                .timestamp(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.GITHUB_PR_ISSUE_EVENT_QUEUE, message);
        log.info("[Github][Webhook] Sent PR/Issue Event to {}", RabbitConfig.GITHUB_PUSH_EVENT_QUEUE);
    }
}
