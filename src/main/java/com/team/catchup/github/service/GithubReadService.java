package com.team.catchup.github.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.team.catchup.github.dto.response.FileTreeNode;
import com.team.catchup.github.dto.response.GithubRepoSummaryResponse;
import com.team.catchup.github.entity.GithubRepository;
import com.team.catchup.github.repository.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GithubReadService {

    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GithubApiService githubApiService;

    /**
     * 저장된 리포지토리 목록 조회 (Name, UpdatedAt)
     */
    public List<GithubRepoSummaryResponse> getRepositorySummaries() {
        return githubRepositoryRepository.findAll().stream()
                .map(GithubRepoSummaryResponse::from)
                .toList();
    }

    /**
     * 리포지토리 파일 트리 조회
     * DB에서 리포지토리 정보를 찾고, GitHub API를 통해 실시간 파일 구조를 가져와 계층형으로 변환합니다.
     */
    public Mono<FileTreeNode> getRepositoryFileTree(Long repositoryId) {
        GithubRepository repository = githubRepositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found: " + repositoryId));

        String targetBranch = repository.getTargetBranch() != null ? repository.getTargetBranch() : "main";

        return githubApiService.getGitTree(repository.getOwner(), repository.getName(), targetBranch)
                .map(jsonNode -> buildFileTree(jsonNode.get("tree"), repository.getName()));
    }

    /**
     * GitHub API 응답을 계층형 트리 구조로 변환
     */
    private FileTreeNode buildFileTree(JsonNode treeNodeArray, String rootName) {
        if (treeNodeArray == null || !treeNodeArray.isArray()) {
            return FileTreeNode.builder()
                    .name(rootName)
                    .type("tree")
                    .path("")
                    .build();
        }

        // 루트 노드 생성
        FileTreeNode root = FileTreeNode.builder()
                .name(rootName)
                .type("tree")
                .path("")
                .build();

        // 경로별 노드 매핑
        Map<String, FileTreeNode> nodeMap = new HashMap<>();
        nodeMap.put("", root);

        // API 응답은 경로순으로 정렬되어 있다고 가정되지만, 안전을 위해 경로 길이로 정렬할 수도 있음
        List<JsonNode> nodes = new ArrayList<>();
        treeNodeArray.forEach(nodes::add);

        // 상위 폴더가 먼저 처리되도록 정렬
        nodes.sort(Comparator.comparing(node -> node.get("path").asText()));

        for (JsonNode node : nodes) {
            String fullPath = node.get("path").asText();
            String type = node.get("type").asText();

            // 파일명과 부모 경로 추출
            int lastSlashIndex = fullPath.lastIndexOf('/');
            String parentPath = (lastSlashIndex == -1) ? "" : fullPath.substring(0, lastSlashIndex);
            String name = (lastSlashIndex == -1) ? fullPath : fullPath.substring(lastSlashIndex + 1);

            FileTreeNode fileNode = FileTreeNode.builder()
                    .name(name)
                    .path(fullPath)
                    .type(type)
                    .build();

            nodeMap.put(fullPath, fileNode);

            // 부모 노드에 현재 노드 추가
            FileTreeNode parent = nodeMap.get(parentPath);
            if (parent != null) {
                parent.addChild(fileNode);
            } else {
                // 부모를 찾지 못한 경우, 루트에 붙이거나 로그 처리
                // 여기서는 루트의 직계 자식으로 처리
                root.addChild(fileNode);
            }
        }

        return root;
    }
}