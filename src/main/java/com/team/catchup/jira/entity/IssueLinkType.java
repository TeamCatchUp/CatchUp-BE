package com.team.catchup.jira.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "linkType", fetch = FetchType.LAZY)
    private List<IssueLink> issueLinks = new ArrayList<>();

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