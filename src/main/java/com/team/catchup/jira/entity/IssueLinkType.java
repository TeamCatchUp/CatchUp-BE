package com.team.catchup.jira.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "issue_link_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueLinkType {

    @Id
    @Column(name = "link_type_id")
    private Integer linkTypeId;

    @Column(name = "name", nullable = false)
    private String name; // Blocks

    @Column(name = "outward")
    private String outward;  // "blocks"

    @Column(name = "inward")
    private String inward;  // "is blocked by"

    @Column(name = "self_url", length = 500)
    private String selfUrl;

    @Builder
    public IssueLinkType(Integer linkTypeId, String name, String inward,
                         String outward, String selfUrl) {
        this.linkTypeId = linkTypeId;
        this.name = name;
        this.inward = inward;
        this.outward = outward;
        this.selfUrl = selfUrl;
    }
}