package com.team.catchup.notion.mapper;

import com.team.catchup.notion.dto.external.NotionUserApiResponse;
import com.team.catchup.notion.entity.NotionUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotionUserMapper {

    public NotionUser toEntity(NotionUserApiResponse.NotionUserResult result) {
        try{
            String email = result.person() != null
                    ? result.person().email()
                    : null;

            return NotionUser.builder()
                    .userId(result.id())
                    .name(result.name())
                    .avatarUrl(result.avatarUrl())
                    .email(email)
                    .build();
        }  catch (Exception e) {
            log.error("Failed to map NotionUser: {}", result.id(), e);
            throw new RuntimeException("NotionUser mapping failed: " + result.id(), e);
        }
    }
}