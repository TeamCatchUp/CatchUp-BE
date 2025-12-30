package com.team.catchup.github.repository;

import com.team.catchup.github.entity.GithubComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubCommentRepository extends JpaRepository<GithubComment, Long> {

    boolean existsByCommentId(Long commentId);

    List<GithubComment> findByIndexedAtIsNull();

    List<GithubComment> findByRepository_RepositoryIdAndIndexedAtIsNull(Long repositoryId);
}
