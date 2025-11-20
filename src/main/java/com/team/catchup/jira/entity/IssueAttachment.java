package com.team.catchup.jira.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "issue_attachment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueAttachment {

    @Id
    @Column(name = "attachment_id")
    private Integer id;

    @Column(name = "issue_id", nullable = false)
    private Integer issueId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "mime_type", nullable = false)
    private String mimetype;

    @Column(name = "download_url", nullable = false)
    private String downloadUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Builder
    public IssueAttachment(Integer id, Integer issueId, String fileName, String authorId, LocalDateTime createdAt,
                           Long size, String mimetype, String downloadUrl, String thumbnailUrl) {
        this.id = id;
        this.issueId = issueId;
        this.fileName = fileName;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.size = size;
        this.mimetype = mimetype;
        this.downloadUrl = downloadUrl;
        this.thumbnailUrl = thumbnailUrl;
    }
}
