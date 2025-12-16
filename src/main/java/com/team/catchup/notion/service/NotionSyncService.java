package com.team.catchup.notion.service;

import com.team.catchup.notion.dto.NotionSyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotionSyncService {

    private final NotionTransactionalService notionTransactionalService;

    public NotionSyncResult syncAll() {
        log.info("[NOTION][FULL SYNC] Full Sync Started");

        try {
            // 1. User
            NotionSyncResult.NotionSyncCount userCount = notionTransactionalService.syncUsers();
            log.info("[NOTION][FULL SYNC] User Sync Completed - Total: {}, Saved: {}",
                    userCount.getTotalFetched(), userCount.getSaved());

            // 2. Page
            NotionSyncResult.NotionSyncCount pageCount = notionTransactionalService.syncPageMetadata();
            log.info("[NOTION][FULL SYNC] Page Sync Completed - Total: {}, Saved: {}",
                    pageCount.getTotalFetched(), pageCount.getSaved());

            return NotionSyncResult.builder()
                    .success(true)
                    .userMetaData(userCount)
                    .pageMetaData(pageCount)
                    .build();
        } catch (Exception e) {
            log.error("[NOTION][ALL] Full Sync Failed", e);
            return NotionSyncResult.failure(e.getMessage());
        }
    }

    public NotionSyncResult syncPageMetadata() {
        log.info("[NOTION][PAGE] Page Metadata Sync Started");

        try {
            NotionSyncResult.NotionSyncCount syncCount = notionTransactionalService.syncPageMetadata();

            log.info("=== [Notion Metadata Sync] SUCCESS - Total: {}, Saved: {} ==="
                    ,syncCount.getTotalFetched(), syncCount.getSaved());

            return NotionSyncResult.success(syncCount);
        } catch (Exception e) {
            log.error("[NOTION][PAGE] Page Metadata Sync Failed", e);
            return NotionSyncResult.failure(e.getMessage());
        }
    }

    public NotionSyncResult syncUsers() {
        log.info("[NOTION][USER] User Sync Started");

        try{
            NotionSyncResult.NotionSyncCount syncCount = notionTransactionalService.syncUsers();

            log.info("=== [Notion User Sync] SUCCESS - Total: {}, Saved: {} ===",
                    syncCount.getTotalFetched(), syncCount.getSaved());

            return NotionSyncResult.builder()
                    .success(true)
                    .userMetaData(syncCount)
                    .build();
        } catch (Exception e) {
            log.error("[NOTION][USER] User Sync Failed", e);
            return NotionSyncResult.failure(e.getMessage());
        }
    }
}

