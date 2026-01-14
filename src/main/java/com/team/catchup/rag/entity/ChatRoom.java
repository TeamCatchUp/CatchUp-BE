package com.team.catchup.rag.entity;

import com.team.catchup.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 시간 자동 저장
public class ChatRoom {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID sessionId;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatHistory> histories = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public ChatRoom(UUID sessionId, String title, Member member) {
        this.sessionId = sessionId;
        this.title = title;
        this.member = member;
    }

    public void updateTitle(String title){
        this.title = title;
    }

    public void updateLastActiveTime() {
        this.updatedAt = LocalDateTime.now();
    }
}
