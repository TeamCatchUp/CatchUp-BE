package com.team.catchup.notion.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "notion_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotionUser {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<NotionPage> createdPages;

    @OneToMany(mappedBy = "lastEditedBy", fetch = FetchType.LAZY)
    private List<NotionPage> editedPages;

    @Builder
    public NotionUser(String userId, String name, String avatarUrl, String email) {
        this.userId = userId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.email = email;
    }
}
