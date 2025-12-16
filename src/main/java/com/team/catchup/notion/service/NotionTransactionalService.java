package com.team.catchup.notion.service;

import com.team.catchup.notion.dto.NotionSearchResponse;
import com.team.catchup.notion.dto.NotionSyncResult;
import com.team.catchup.notion.dto.NotionUserResponse;
import com.team.catchup.notion.entity.NotionPage;
import com.team.catchup.notion.entity.NotionUser;
import com.team.catchup.notion.mapper.NotionPageMapper;
import com.team.catchup.notion.mapper.NotionUserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotionTransactionalService {

    private final NotionApiService notionApiService;
    private final NotionSavingSevice notionSavingSevice;
    private final NotionPageMapper notionPageMapper;
    private final NotionUserMapper notionUserMapper;

    @Transactional
    public NotionSyncResult.NotionSyncCount syncPageMetadata() {
        log.info("[NOTION][Page] Page Metadata Sync Started");

        List<NotionSearchResponse.NotionPageResult> results = notionApiService
                .fetchAllPages()
                .block();

        if(results == null || results.isEmpty()) {
            log.info("[NOTION][Page] Page Search Result is EMPTY");
            return NotionSyncResult.NotionSyncCount.empty();
        }

        int totalFetched = results.size();
        log.info("[NOTION][Page] Page Metadata Search Completed - Pages :{}", totalFetched);

        List<NotionPage> pages = results.stream()
                .map(notionPageMapper::toEntity)
                .toList();

        int savedCount = notionSavingSevice.saveAllPages(pages);

        log.info("[NOTION][Page] Page Metadata Save Completed - Pages :{}", savedCount);
        return NotionSyncResult.NotionSyncCount.of(totalFetched, savedCount);
    }

    @Transactional
    public NotionSyncResult.NotionSyncCount syncUsers() {
        log.info("[NOTION][User] User Sync Started");

        List<NotionUserResponse.NotionUserResult> results = notionApiService
                .fetchAllUsers()
                .block();

        if(results == null || results.isEmpty()) {
            log.info("[NOTION][User] User Search Result is EMPTY");
            return NotionSyncResult.NotionSyncCount.empty();
        }

        int totalFetched = results.size();
        log.info("[NOTION][User] User Fetch Completed :{}", totalFetched);

        List<NotionUser> users = results.stream()
                .map(notionUserMapper::toEntity)
                .toList();

        int savedCount = notionSavingSevice.saveAllUsers(users);

        log.info("[NOTION][User] User Save Completed :{}", savedCount);
        return NotionSyncResult.NotionSyncCount.of(totalFetched, savedCount);
    }
}
