package com.team.catchup.notion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notion")
public class NotionProperties {

    private String baseUrl;
    private String apiKey;
    private String version;

    private ConnectionConfig connection = new ConnectionConfig();
    private TimeoutConfig timeout = new TimeoutConfig();
    private MemoryConfig memory = new MemoryConfig();

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
        private Integer read;
        private Integer write;
    }

    @Getter
    @Setter
    public static class MemoryConfig {
        private Integer maxInMemorySize;
    }
}