package com.team.catchup.rag.entity;

import com.team.catchup.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "chat_history_id"})
})
public class ChatFeedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_feedback_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_history_id", nullable = false)
    private ChatHistory chatHistory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb", nullable = false)
    private List<String> tags;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Builder
    private ChatFeedback(Long memberId, ChatHistory chatHistory, List<String> tags, String detail) {
        this.memberId = memberId;
        this.chatHistory = chatHistory;
        this.tags = tags;
        this.detail = detail;
    }
}
