package com.team.catchup.github.dto.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.github.entity.GithubRepository;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GithubRepositoryRabbitRequest {

    @JsonProperty("repository_id")
    private Long repositoryId;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("repo_name")
    private String repoName;

    @JsonProperty("branch")
    private String branch;

    @JsonProperty("github_token")
    private String githubToken;

    public static GithubRepositoryRabbitRequest from(GithubRepository repository) {
        return GithubRepositoryRabbitRequest.builder()
            .repositoryId(repository.getRepositoryId())
            .owner(repository.getOwner())
            .repoName(repository.getName())
            .branch(repository.getTargetBranch())
            .githubToken(null) // TODO: Private 저장소용 토큰이 필요한 경우 추가
            .build();
    }
}
