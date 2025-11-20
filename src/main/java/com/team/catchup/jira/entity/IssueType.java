package com.team.catchup.jira.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueType {

    @Id
    @Column(name = "issue_type_id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon_url", nullable = false)
    private String iconUrl;

    @Column(name = "is_subtask", nullable = false)
    private boolean isSubtask;

    @Column(name = "hierarchy_level", nullable = false)
    private Integer hierarchyLevel;

    // Cutom Issue Type일 경우에 Scope 컬럼이 추가됨
    // Global 혹은 Project으로 저장 예정 -> 추후에 enum으로 수정하겠습니다
    @Column(name = "scope_type", nullable = false)
    private String scopeType;

    // 해당 커스텀 이슈를 적용할 있는 프로젝트의 아이디입니다.
    @Column(name = "scope_project_id")
    private Integer scopeProjectId;

    @OneToMany(mappedBy = "issueType", fetch = FetchType.LAZY)
    private List<IssueMetadata> issueMetadata = new ArrayList<>();

    @Builder
    public IssueType(Integer id, String name, String iconUrl,
                     Boolean isSubtask, Integer hierarchyLevel,
                     String scopeType, Integer scopeProjectId) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.isSubtask = isSubtask;
        this.hierarchyLevel = hierarchyLevel;
        this.scopeType = scopeType;
        this.scopeProjectId = scopeProjectId;
    }

    public boolean isGlobal() {
        return this.scopeType.equals("Global");
    }

    public boolean isProjectScope() {
        return this.scopeType.equals("Project");
    }
}
