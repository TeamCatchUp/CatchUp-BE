package com.team.catchup.github.service.webhook;

import com.team.catchup.github.config.WebhookProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookSignatureValidator {
    private final WebhookProperties webhookProperties;

    public boolean validateSignature(String payload, String signature) {
        if(signature == null || !signature.startsWith("sha256=")) {
            return false;
        }

        try {
            String secret = webhookProperties.getSecret();
            String expectedSignature = "sha256=" + hmacSha256(payload, secret);

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("[Github][Webhook] Signature Validation Error", e);
            return false;
        }
    }

    private String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for(byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
