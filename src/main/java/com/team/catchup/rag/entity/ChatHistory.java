package com.team.catchup.rag.entity;

import com.team.catchup.member.entity.Member;
import com.team.catchup.rag.entity.vo.ChatMetadata;
import com.team.catchup.rag.dto.server.ServerChatResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_history_seq")
    @SequenceGenerator(name = "chat_history_seq", sequenceName = "chat_history_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(columnDefinition = "TEXT")
    private String content; // 대화 본문

    private String role; // user, assistant

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private ChatMetadata metadata;

    private LocalDateTime createdAt;

    @Builder
    private ChatHistory(
            ChatRoom chatRoom,
            Member member,
            String content,
            String role,
            ChatMetadata metadata
    ) {
        this.chatRoom = chatRoom;
        this.member = member;
        this.content = content;
        this.role = role;
        this.metadata = metadata;
        this.createdAt = LocalDateTime.now();
    }

    public static ChatHistory createUserInfo(
            ChatRoom chatRoom,
            Member member,
            String query,
            List<String> indexList
    ) {
        return ChatHistory.builder()
                .chatRoom(chatRoom)
                .member(member)
                .content(query)
                .role("user")
                .metadata(
                        ChatMetadata.builder()
                                .indexList(indexList)
                                .build())
                .build();
    }

    // Assistant
    public static ChatHistory createAssistantInfo(
            ChatRoom chatRoom,
            Member member,
            ServerChatResponse response
    ) {
        return ChatHistory.builder()
                .chatRoom(chatRoom)
                .member(member)
                .content(response.answer())
                .role("assistant")
                .metadata(ChatMetadata.builder()
                        .serverSources(response.sources())
                        .processTime(response.processTime())
                        .build())
                .build();
    }
}
