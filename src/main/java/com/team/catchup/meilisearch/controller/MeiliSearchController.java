package com.team.catchup.meilisearch.controller;

import com.team.catchup.meilisearch.document.MeiliSearchDocument;
import com.team.catchup.meilisearch.dto.MeiliSearchDocumentRequest;
import com.team.catchup.meilisearch.dto.MeiliSearchQueryResponse;
import com.team.catchup.meilisearch.service.MeiliSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MeiliSearchController {

    private final MeiliSearchService meiliSearchService;

    /**
     * Document 생성 또는 갱신
     */
    @PostMapping("/api/documents")
    public ResponseEntity<Void> addOrUpdateDocument (
            @RequestBody MeiliSearchDocumentRequest request
    ){
        List<MeiliSearchDocument> documents = request.documents();
        meiliSearchService.addOrUpdateDocument(documents);
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
