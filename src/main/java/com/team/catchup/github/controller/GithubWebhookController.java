package com.team.catchup.github.controller;

import com.team.catchup.github.service.webhook.WebhookEventService;
import com.team.catchup.github.service.webhook.WebhookSignatureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/github/webhook")
@RequiredArgsConstructor
public class GithubWebhookController {

    private final WebhookSignatureValidator signatureValidator;
    private final WebhookEventService webhookEventService;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-Github-Event") String eventType,
            @RequestHeader("X-Hub-Signature-256") String signature,
            @RequestBody String payload) {

        if(!signatureValidator.validateSignature(payload, signature)) {
            log.warn("[Github][Webhook] Invalid signature received");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature received");
        }

        try {
            switch (eventType) {
                case "push":
                    webhookEventService.handlePushEvent(payload);
                    break;
                case "pull_request":
                case "issues":
                    webhookEventService.handlePrIssueEvent(eventType, payload);
                    break;
                default:
                    log.debug("[Webhook] Unsupported event type: {}", eventType);
                    return ResponseEntity.ok("Event ignored");
            }

            return ResponseEntity.ok("Processed");

        } catch (Exception e) {
            log.error("[Webhook] Processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }
}
