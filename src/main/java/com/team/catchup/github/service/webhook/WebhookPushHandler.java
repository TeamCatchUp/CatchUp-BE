package com.team.catchup.github.service.webhook;

import com.team.catchup.github.dto.webhook.WebhookPushPayload;
import com.team.catchup.github.entity.GithubCommit;
import com.team.catchup.github.entity.GithubFileChange;
import com.team.catchup.github.entity.GithubRepository;
import com.team.catchup.github.repository.GithubCommitRepository;
import com.team.catchup.github.repository.GithubFileChangeRepository;
import com.team.catchup.github.repository.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookPushHandler {

    private final GithubRepositoryRepository repositoryRepository;
    private final GithubCommitRepository commitRepository;
    private final GithubFileChangeRepository fileChangeRepository;

    /**
     * Push 이벤트를 처리하고 변경된 파일 경로 목록을 반환
     * @param payload Push webhook payload
     * @return 변경된 파일 경로 목록
     */
    public List<String> handlePushEvent(WebhookPushPayload payload) {

        String owner = payload.getRepository().getOwner().getLogin();
        String repo = payload.getRepository().getName();
        Long repositoryId = payload.getRepository().getId();
        String branchName = payload.getBranchName();

        // 기존에 Full Sync가 완료되지 않은 repo_branch에 대해서는 Webhook 이벤트를 처리하지 않음
        GithubRepository repository = repositoryRepository.findByOwnerAndName(owner, repo)
                .orElseThrow(() -> {
                    log.error("[Webhook][Push] Repository {}/{} not found. Full sync required.", owner, repo);
                    return new IllegalStateException();
                });

        // targetBranch에 대한 push만 처리
        if (!branchName.equals(repository.getTargetBranch())) {
            log.info("[Webhook][Push] Ignoring push to non-target branch {} (target: {}) for {}/{}",
                    branchName, repository.getTargetBranch(), owner, repo);
            return List.of();
        }

        // Commit 메타데이터 저장
        List<WebhookPushPayload.Commit> commits = payload.getCommits();
        if (commits == null || commits.isEmpty()) {
            log.info("[Webhook][Push] No commits to process for {}/{}", owner, repo);
            return List.of();
        }

        List<String> commitShas = commits.stream()
                .map(WebhookPushPayload.Commit::getId)
                .distinct()
                .collect(Collectors.toList());

        // 이미 존재하는 커밋 필터링
        Set<String> existingShas = commitRepository
                .findByRepository_RepositoryIdAndShaIn(repositoryId, commitShas)
                .stream()
                .map(GithubCommit::getSha)
                .collect(Collectors.toSet());

        List<String> newCommitShas = commitShas.stream()
                .filter(sha -> !existingShas.contains(sha))
                .collect(Collectors.toList());

        if (newCommitShas.isEmpty()) {
            log.info("[Webhook][Push] All {} commits already exist for {}/{}",
                    commits.size(), owner, repo);
            return List.of();
        }

        // 새로운 커밋만 저장
        List<GithubCommit> newCommits = commits.stream()
                .filter(commit -> newCommitShas.contains(commit.getId()))
                .map(commit -> {
                    GithubCommit.GithubCommitBuilder builder = GithubCommit.builder()
                            .repository(repository)
                            .sha(commit.getId())
                            .message(commit.getMessage())
                            .htmlUrl(commit.getUrl());

                    if (commit.getAuthor() != null) {
                        builder.authorName(commit.getAuthor().getName())
                               .authorEmail(commit.getAuthor().getEmail());
                    }

                    if (commit.getTimestamp() != null) {
                        try {
                            LocalDateTime authorDate = ZonedDateTime.parse(commit.getTimestamp())
                                    .toLocalDateTime();
                            builder.authorDate(authorDate);
                        } catch (Exception e) {
                            log.warn("[Webhook][Push] Failed to parse timestamp for commit {}: {}",
                                    commit.getId(), commit.getTimestamp());
                        }
                    }

                    return builder.build();
                })
                .collect(Collectors.toList());

        commitRepository.saveAll(newCommits);

        log.info("[Webhook][Push] Saved {}/{} new commits for {}/{}",
                newCommits.size(), commits.size(), owner, repo);

        // 파일 변경사항 저장 및 변경된 파일 경로 수집
        Set<String> changedFilePaths = new HashSet<>();
        List<GithubFileChange> fileChanges = new ArrayList<>();

        for (WebhookPushPayload.Commit commit : commits) {
            if (!newCommitShas.contains(commit.getId())) {
                continue;  // 이미 존재하는 커밋은 스킵
            }

            String commitSha = commit.getId();

            // Added files
            if (commit.getAdded() != null) {
                for (String filePath : commit.getAdded()) {
                    changedFilePaths.add(filePath);
                    fileChanges.add(GithubFileChange.builder()
                            .repository(repository)
                            .commitSha(commitSha)
                            .filePath(filePath)
                            .changeType(GithubFileChange.FileChangeType.ADDED)
                            .build());
                }
            }

            // Modified files
            if (commit.getModified() != null) {
                for (String filePath : commit.getModified()) {
                    changedFilePaths.add(filePath);
                    fileChanges.add(GithubFileChange.builder()
                            .repository(repository)
                            .commitSha(commitSha)
                            .filePath(filePath)
                            .changeType(GithubFileChange.FileChangeType.MODIFIED)
                            .build());
                }
            }

            // Removed files
            if (commit.getRemoved() != null) {
                for (String filePath : commit.getRemoved()) {
                    changedFilePaths.add(filePath);
                    fileChanges.add(GithubFileChange.builder()
                            .repository(repository)
                            .commitSha(commitSha)
                            .filePath(filePath)
                            .changeType(GithubFileChange.FileChangeType.DELETED)
                            .build());
                }
            }
        }

        if (!fileChanges.isEmpty()) {
            fileChangeRepository.saveAll(fileChanges);
            log.info("[Webhook][Push] Saved {} file changes for {}/{}",
                    fileChanges.size(), owner, repo);
        }

        return new ArrayList<>(changedFilePaths);
    }
}
