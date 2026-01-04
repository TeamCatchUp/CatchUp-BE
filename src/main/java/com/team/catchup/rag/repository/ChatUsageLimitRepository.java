package com.team.catchup.rag.repository;

import com.team.catchup.rag.entity.ChatUsageLimit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ChatUsageLimitRepository extends JpaRepository<ChatUsageLimit, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatUsageLimit> findByMemberIdAndUsageDate(Long memberId, LocalDate usageDate);
}
