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
}

