package com.team.catchup.github.mapper;

import com.team.catchup.github.entity.GithubCommit;
import com.team.catchup.github.entity.GithubCommitParent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class GithubCommitParentMapper {

    public List<GithubCommitParent> toEntities(List<String> parentShas, GithubCommit commit) {
        List<GithubCommitParent> parents = new ArrayList<>();

        for (int i = 0; i < parentShas.size(); i++) {
            parents.add(GithubCommitParent.builder()
                    .commit(commit)
                    .parentSha(parentShas.get(i))
                    .parentOrder(i)
                    .build());
        }

        return parents;
    }
}
