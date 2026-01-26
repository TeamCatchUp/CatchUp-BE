package com.team.catchup.github.controller;

import com.team.catchup.github.dto.response.FileTreeNode;
import com.team.catchup.github.dto.response.GithubRepoSummaryResponse;
import com.team.catchup.github.service.GithubReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/github/read")
@RequiredArgsConstructor
public class GithubReadController {

    private final GithubReadService githubReadService;

    /**
     * 저장된 Github Repository 목록 조회
     * Response : repositoryId, name, updatedAt 등
     */
    @GetMapping("/repositories")
    public ResponseEntity<List<GithubRepoSummaryResponse>> getRepositories() {
        List<GithubRepoSummaryResponse> response = githubReadService.getRepositorySummaries();
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 Repository의 파일 트리 구조 조회
     * Response : 계층형 JSON
     */
    @GetMapping("/repositories/{repositoryId}/files")
    public Mono<ResponseEntity<FileTreeNode>> getRepositoryFileTree(
            @PathVariable Long repositoryId
    ) {
        return githubReadService.getRepositoryFileTree(repositoryId)
                .map(ResponseEntity::ok);
    }
}