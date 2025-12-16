package com.team.catchup.notion.mapper;

import com.team.catchup.notion.dto.NotionUserResponse;
import com.team.catchup.notion.entity.NotionUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotionUserMapper {

    public NotionUser toEntity(NotionUserResponse.NotionUserResult result) {
        String email = null;

        if (result.person() != null) {
            email = result.person().email();
        }

        return NotionUser.builder()
                .userId(result.id())
                .name(result.name())
                .avatarUrl(result.avatarUrl())
                .email(email)
                .build();
    }
}