package com.team.catchup.github.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "github")
public class GithubProperties {

    private String baseUrl;
    private String token;
    private String apiVersion;
    private ConnectionProperties connection;
    private TimeoutProperties timeout;
    private MemoryProperties memory;

    @Getter
    @Setter
    public static class ConnectionProperties {
        private Integer maxConnections;
        private Integer pendingAcquireTimeout;
    }

    @Getter
    @Setter
    public static class TimeoutProperties {
        private Integer connect;
        private Integer read;
        private Integer write;
    }

    @Getter
    @Setter
    public static class MemoryProperties {
        private Integer maxInMemorySize;
    }
}
