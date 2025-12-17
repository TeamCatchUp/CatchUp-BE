package com.team.catchup.notion.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotionSyncCount {
    private int totalFetched;
    private int saved;
    private int skipped;

    public static NotionSyncCount of(int totalFetched, int saved) {
        return NotionSyncCount.builder()
                .totalFetched(totalFetched)
                .saved(saved)
                .skipped(totalFetched - saved)
                .build();
    }

    public static NotionSyncCount empty() {
        return NotionSyncCount.of(0, 0);
    }
}
