package com.team.catchup.rag.mapper;

import com.team.catchup.rag.dto.internal.CommitInfo;
import com.team.catchup.rag.dto.client.*;
import com.team.catchup.rag.dto.server.ServerCodeSource;
import com.team.catchup.rag.dto.server.ServerJiraIssueSource;
import com.team.catchup.rag.dto.server.ServerPullRequestSource;
import com.team.catchup.rag.dto.server.ServerSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class ClientSourceMapper {

    public ClientSource map(ServerSource source, CommitInfo commitInfo) {
        if (source instanceof ServerCodeSource codeSource) {
            if (commitInfo == null) {
                return ClientCodeSource.of(codeSource, null, "Unknown", null);
            }
            return ClientCodeSource.of(
                    codeSource,
                    commitInfo.message(),
                    commitInfo.author(),
                    calculateDaysAgo(commitInfo.date())
            );
        }

        else if (source instanceof ServerPullRequestSource prSource) {
            return ClientPullRequestSource.from(prSource);
        }

        else if (source instanceof ServerJiraIssueSource jiraIssueSource) {
            return ClientJiraIssueSource.from(jiraIssueSource);
        }

        throw new IllegalArgumentException("지원하지 않는 Source Type 입니다.: " + source.getClass().getName());
    }

    private Integer calculateDaysAgo(LocalDateTime date) {
        if (date == null) return null;
        return (int) ChronoUnit.DAYS.between(date, LocalDateTime.now());
    }
}
