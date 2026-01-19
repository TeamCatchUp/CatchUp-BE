package com.team.catchup.github.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "github.webhook")
@Getter
@Setter
public class WebhookProperties {

    private String secret;
    private Batch batch = new Batch();

    @Getter
    @Setter
    public static class Batch {
        private int size = 10;
        private int intervalMs = 5000;
    }
}
