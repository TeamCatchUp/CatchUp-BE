package com.team.catchup.jira.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SyncCount {
    private final int total;
    private final int saved;
    private final int skipped;

    public static SyncCount of(int total, int saved) {
        return SyncCount.builder()
                .total(total)
                .saved(saved)
                .skipped(total - saved)
                .build();
    }

    public static SyncCount empty() {
        return SyncCount.builder()
                .total(0)
                .saved(0)
                .skipped(0)
                .build();
    }
}