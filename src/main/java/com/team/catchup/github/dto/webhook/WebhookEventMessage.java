package com.team.catchup.github.dto.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEventMessage implements Serializable {

    private String eventType;   // push, pull_request, issue
    private String action;      // opened, closed, edited ...

    private Long repositoryId;
    private String owner;
    private String repo;
    private LocalDateTime timestamp;

    // Push Event
    private String ref; // refs/heads/{branchName}
    private List<String> commitShas;

    // PR + Issue Event
    private Integer issueNumber;
    private String title;

    public String getBranchName() {
        return ref!= null ? ref.replace("refs/heads/", "") : "";
    }

}
