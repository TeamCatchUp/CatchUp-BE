package com.team.catchup.github.repository;

import com.team.catchup.github.entity.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GithubRepositoryRepository extends JpaRepository<GithubRepository, Long> {

    Optional<GithubRepository> findByOwnerAndName(String owner, String name);

    boolean existsByOwnerAndName(String owner, String name);
}
