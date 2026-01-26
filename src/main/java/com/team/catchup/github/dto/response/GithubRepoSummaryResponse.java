package com.team.catchup.github.dto.response;

import com.team.catchup.github.entity.GithubRepository;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GithubRepoSummaryResponse {
    private Long repositoryId;
    private String name;
    private String owner;
    private LocalDateTime updatedAt;

    public static GithubRepoSummaryResponse from(GithubRepository repository) {
        return GithubRepoSummaryResponse.builder()
                .repositoryId(repository.getRepositoryId())
                .name(repository.getName())
                .owner(repository.getOwner())
                .updatedAt(repository.getUpdatedAt())
                .build();
    }
}