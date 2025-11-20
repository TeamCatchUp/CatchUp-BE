package com.team.catchup.jira.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jira_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JiraUser {

    @Id
    @Column(name = "account_id")
    private String accountId;

    @Column(name = "account_type",  nullable = false)
    private String accountType;  // "atlassian", "app", "customer"

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "locale")
    private String locale;

    @Column(name = "self_url", length = 500)
    private String selfUrl;

    @Builder
    public JiraUser(String accountId, String accountType, String displayName,
                    String avatarUrl, Boolean active,
                    String locale, String selfUrl) {
        this.accountId = accountId;
        this.accountType = accountType;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.active = active;
        this.locale = locale;
        this.selfUrl = selfUrl;
    }
}