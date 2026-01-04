package com.team.catchup.rag.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "usage_date"})
})
public class ChatUsageLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_usage_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount;

    public void incrementUsageCount() {
        this.usageCount++;
    }

    public static ChatUsageLimit createNewUsage(Long memberId) {
        return ChatUsageLimit.builder()
                .memberId(memberId)
                .usageDate(LocalDate.now())
                .usageCount(1)
                .build();
    }
}
