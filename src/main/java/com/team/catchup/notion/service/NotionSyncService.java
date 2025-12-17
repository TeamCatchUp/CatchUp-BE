package com.team.catchup.notion.service;

import com.team.catchup.notion.dto.NotionSyncCount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotionSyncService {

    private final NotionProcessor notionProcessor;

    @Async
    public void syncAll() {
        log.info("[NOTION][FULL SYNC] Full Sync Started");
        long startTime = System.currentTimeMillis();

        try {
            // 1. User
            NotionSyncCount userCount = notionProcessor.syncUsers();
            log.info("[NOTION][FULL SYNC] User Sync Completed - Total: {}, Saved: {}",
                    userCount.getTotalFetched(), userCount.getSaved());

            // 2. Page
            NotionSyncCount pageCount = notionProcessor.syncPageMetadata();
            log.info("[NOTION][FULL SYNC] Page Sync Completed - Total: {}, Saved: {}",
                    pageCount.getTotalFetched(), pageCount.getSaved());

            long duration = System.currentTimeMillis() - startTime;
            log.info("[NOTION][FULL SYNC] Sync Completed - Duration: {}", duration);
        } catch (Exception e) {
            log.error("[NOTION][ALL] Full Sync Failed", e);
        }
    }

    @Async
    public void syncPageMetadata() {
        log.info("[NOTION][PAGE] Page Metadata Sync Started");

        try {
            NotionSyncCount syncCount = notionProcessor.syncPageMetadata();

            log.info("[Notion Metadata Sync] SUCCESS - Total: {}, Saved: {} ==="
                    ,syncCount.getTotalFetched(), syncCount.getSaved());

        } catch (Exception e) {
            log.error("[NOTION][PAGE] Page Metadata Sync Failed", e);
        }
    }

    @Async
    public void syncUsers() {
        log.info("[NOTION][USER] User Sync Started");

        try{
            NotionSyncCount syncCount = notionProcessor.syncUsers();

            log.info("=== [Notion User Sync] SUCCESS - Total: {}, Saved: {} ===",
                    syncCount.getTotalFetched(), syncCount.getSaved());

        } catch (Exception e) {
            log.error("[NOTION][USER] User Sync Failed", e);
        }
    }
}

