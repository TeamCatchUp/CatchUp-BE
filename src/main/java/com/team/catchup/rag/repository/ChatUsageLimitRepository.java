package com.team.catchup.rag.repository;

import com.team.catchup.rag.entity.ChatUsageLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ChatUsageLimitRepository extends JpaRepository<ChatUsageLimit, Long> {
    Optional<ChatUsageLimit> findByMemberIdAndUsageDate(Long memberId, LocalDate usageDate);
}
