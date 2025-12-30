package com.team.catchup.github.repository;

import com.team.catchup.github.entity.GithubFileChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubFileChangeRepository extends JpaRepository<GithubFileChange, Long> {

    List<GithubFileChange> findByCommitSha(String commitSha);

    List<GithubFileChange> findByPullRequest_PullRequestId(Long pullRequestId);

    List<GithubFileChange> findByFilePath(String filePath);
}
