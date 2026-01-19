package com.team.catchup.meilisearch.controller;

import com.team.catchup.meilisearch.dto.MeiliSearchQueryResponse;
import com.team.catchup.meilisearch.service.MeiliSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MeiliSearchController {

    private final MeiliSearchService meiliSearchService;

    /**
     * Document 생성 또는 갱신
     */
    @PostMapping("/api/documents")
    public ResponseEntity<Void> syncDocuments() {
        meiliSearchService.syncAllJiraIssues();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Document 검색 - Multi Index Search 지원
     */
    @GetMapping("/api/search")
    public ResponseEntity<MeiliSearchQueryResponse> search (
            @RequestParam String query,
            @RequestParam List<String> indices

    ) {
        MeiliSearchQueryResponse response = meiliSearchService.search(query, indices);
        return ResponseEntity.ok(response);
    }

}
