package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.external.JiraUserApiResponse;
import com.team.catchup.jira.entity.JiraUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JiraUserMapper {

    public JiraUser toEntity(JiraUserApiResponse response) {
        try {
            // avatarUrls에서 48x48 이미지 URL 추출
            String avatarUrl = response.avatarUrls().avatarUrl();

            return JiraUser.builder()
                    .accountId(response.accountId())
                    .accountType(response.accountType())
                    .displayName(response.displayName())
                    .avatarUrl(avatarUrl)
                    .active(response.active() != null ? response.active() : false)
                    .locale(response.locale())
                    .selfUrl(response.self())
                    .build();

        } catch (Exception e) {
            log.error("Failed to map JiraUser: {}", response.accountId(), e);
            throw new RuntimeException("JiraUser mapping failed: " + response.accountId(), e);
        }
    }
}