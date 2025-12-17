package com.team.catchup.notion.controller;

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
    public ResponseEntity<String> syncPageMetadata() {
        log.info("[API] Notion Metadata Sync 요청");
        notionSyncService.syncPageMetadata();
        return ResponseEntity.ok("Notion Page Sync Process Started at Background");
    }

    @PostMapping("/users")
    public ResponseEntity<String> syncUsers() {
        log.info("[API] Notion User Sync 요청");
        notionSyncService.syncUsers();
        return ResponseEntity.ok("Notion User Sync Process Started at Background");
    }

    @PostMapping("/full")
    public ResponseEntity<String> fullSync() {
        log.info("[API] Notion Full Sync 요청");
        notionSyncService.syncAll();
        return ResponseEntity.ok("Notion Full Sync Started at Background");
    }
}