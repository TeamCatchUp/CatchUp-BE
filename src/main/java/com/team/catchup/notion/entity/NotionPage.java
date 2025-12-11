package com.team.catchup.notion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notion_page")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotionPage {

    @Id
    @Column(name = "page_id")
    private String pageId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "created_by_id")
    private String createdBy;

    @Column(name = "last_edited_by_id")
    private String lastEditedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_edited_at")
    private LocalDateTime lastEditedAt;

    // 최상위 페이지는 parent Id 없음 !
    @Column(name = "parent_id")
    private String parentId;

    // Workspace, Page, DataSource
    // WorkSpace : 부모가 워크스페이스 -> 워크 스페이스 내의 최상위 페이지
    // Page : 다른 페이지 아래에 딸려있는 페이지
    // Data Source : 데이터베이스에 위치하고 있는 페이지
    @Column(name = "parent_type")
    private String parentType;

    @Builder
    NotionPage(String pageId, String title, String url, String createdBy, String lastEditedBy,
               LocalDateTime createdAt, LocalDateTime lastEditedAt, String parentId, String parentType) {
        this.pageId = pageId;
        this.title = title;
        this.url = url;
        this.createdBy = createdBy;
        this.lastEditedBy = lastEditedBy;
        this.createdAt = createdAt;
        this.lastEditedAt = lastEditedAt;
        this.parentId = parentId;
        this.parentType = parentType;
    }
}
