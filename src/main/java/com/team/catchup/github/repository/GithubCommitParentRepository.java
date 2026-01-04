package com.team.catchup.github.repository;

import com.team.catchup.github.entity.GithubCommitParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubCommitParentRepository extends JpaRepository<GithubCommitParent, Long> {
}
