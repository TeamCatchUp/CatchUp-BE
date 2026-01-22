package com.team.catchup.github.service;

import com.team.catchup.github.entity.GithubCommit;
import com.team.catchup.github.repository.GithubCommitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubCommitService {

    private final GithubCommitRepository commitRepository;

    @Transactional(readOnly = true)
    public GithubCommit getLatestCommit(String filePath) {
        List<GithubCommit> commits = commitRepository.findLatestByFilePath(
                filePath,
                PageRequest.of(0, 1)
        );

        return commits.isEmpty() ? null : commits.get(0);
    }
}
