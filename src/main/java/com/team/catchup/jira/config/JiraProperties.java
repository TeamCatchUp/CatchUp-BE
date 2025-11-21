package com.team.catchup.jira.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
// application.yml의 jira: 설정값을 자동으로 바인딩
@ConfigurationProperties(prefix = "jira")
public class JiraProperties {
    private String baseUrl;
    private String email;
    private String apiToken;

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
