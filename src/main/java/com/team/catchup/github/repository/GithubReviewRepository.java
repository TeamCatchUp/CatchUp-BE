package com.team.catchup.github.repository;

import com.team.catchup.github.entity.GithubReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GithubReviewRepository extends JpaRepository<GithubReview, Long> {

    boolean existsByReviewId(Long reviewId);

    List<GithubReview> findByIndexedAtIsNull();

    List<GithubReview> findByRepository_RepositoryIdAndIndexedAtIsNull(Long repositoryId);
}
