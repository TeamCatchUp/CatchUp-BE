package com.team.catchup.rag.config;

import com.team.catchup.jira.config.JiraProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rag")
public class RagProperties {
    private String baseUrl;

    private RagProperties.ConnectionConfig connection = new RagProperties.ConnectionConfig();
    private RagProperties.TimeoutConfig timeout = new RagProperties.TimeoutConfig();
    private RagProperties.MemoryConfig memory = new RagProperties.MemoryConfig();

    @Getter
    @Setter
    public static class ConnectionConfig {
        private Integer maxConnections;
        private Integer pendingAcquireTimeout;
    }

    @Getter
    @Setter
    public static class TimeoutConfig {
        private Integer connect;
        private Integer response;
        private Integer read;
        private Integer write;
    }

    @Getter
    @Setter
    public static class MemoryConfig {
        private Integer maxInMemorySize;
    }
}
