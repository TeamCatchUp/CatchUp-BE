package com.team.catchup.notion.service;

import com.team.catchup.notion.dto.NotionSearchResponse;
import com.team.catchup.notion.dto.NotionSyncResult;
import com.team.catchup.notion.entity.NotionPage;
import com.team.catchup.notion.mapper.NotionPageMapper;
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
}
