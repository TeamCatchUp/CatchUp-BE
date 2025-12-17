package com.team.catchup.notion.service;

import com.team.catchup.notion.dto.*;
import com.team.catchup.notion.entity.NotionPage;
import com.team.catchup.notion.entity.NotionUser;
import com.team.catchup.notion.mapper.NotionPageMapper;
import com.team.catchup.notion.mapper.NotionUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotionProcessor {

    private final RabbitTemplate rabbitTemplate;
    private static final String NOTION_SYNC_QUEUE = "notion_sync_queue";

    private final NotionApiService notionApiService;
    private final NotionPersistenceService notionPersistenceService;
    private final NotionPageMapper notionPageMapper;
    private final NotionUserMapper notionUserMapper;

    public NotionSyncCount syncPageMetadata() {
        log.info("[NOTION][Page] Page Metadata Sync Started");

        List<NotionSearchResponse.NotionPageResult> results = notionApiService
                .fetchAllPages()
                .block();

        if(results == null || results.isEmpty()) {
            log.info("[NOTION][Page] Page Search Result is EMPTY");
            return NotionSyncCount.empty();
        }

        int totalFetched = results.size();
        log.info("[NOTION][Page] Page Metadata Search Completed - Pages :{}", totalFetched);

        List<NotionPage> pages = results.stream()
                .map(notionPageMapper::toEntity)
                .toList();

        int savedCount = notionPersistenceService.saveAllPages(pages);
        log.info("[NOTION][Page] Page Metadata Save Completed - Pages :{}", savedCount);

        publishSyncMessages(pages);

        return NotionSyncCount.of(totalFetched, savedCount);
    }

    public NotionSyncCount syncUsers() {
        log.info("[NOTION][User] User Sync Started");

        List<NotionUserResponse.NotionUserResult> results = notionApiService
                .fetchAllUsers()
                .block();

        if(results == null || results.isEmpty()) {
            log.info("[NOTION][User] User Search Result is EMPTY");
            return NotionSyncCount.empty();
        }

        int totalFetched = results.size();
        log.info("[NOTION][User] User Fetch Completed :{}", totalFetched);

        List<NotionUser> users = results.stream()
                .map(notionUserMapper::toEntity)
                .toList();

        int savedCount = notionPersistenceService.saveAllUsers(users);

        log.info("[NOTION][User] User Save Completed :{}", savedCount);
        return NotionSyncCount.of(totalFetched, savedCount);
    }

    private void publishSyncMessages(List<NotionPage> pages) {
        try{
            log.info("[NOTION][MQ] Publishing {} pages to RabbitMQ", pages.size());

            for(NotionPage page : pages) {
                NotionRabbitRequest request = NotionRabbitRequest.from(page);

                rabbitTemplate.convertAndSend(NOTION_SYNC_QUEUE, request);
            }
            log.info("[NOTION][MQ] Published {} pages to RabbitMQ", pages.size());
        } catch(Exception e){
            log.error("[NOTION][MQ] Publishing failed", e);
        }
    }
}
