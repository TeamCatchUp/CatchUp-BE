package com.team.catchup.notion.controller;

import com.team.catchup.notion.dto.NotionSyncResult;
import com.team.catchup.notion.service.NotionSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/notion/sync")
@RequiredArgsConstructor
public class NotionSyncController {

    private final NotionSyncService notionSyncService;

    /**
     * Notion 페이지 메타데이터 동기화
     * * POST /api/notion/sync/metadata
     */
    @PostMapping("/metadata")
    public ResponseEntity<NotionSyncResult> syncPageMetadata() {
        log.info("[API] Notion Metadata Sync 요청");
        NotionSyncResult result = notionSyncService.syncPageMetadata();
        return ResponseEntity.ok(result);
    }
}