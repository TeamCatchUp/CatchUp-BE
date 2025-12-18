package com.team.catchup.common.sse.dto;

import com.team.catchup.notion.dto.NotionSyncStep;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotionSyncProgress {
    private NotionSyncStep step;
    private SyncCount count;
    private String currentPageKey;
    private String message;

    public static NotionSyncProgress of (NotionSyncStep step, SyncCount count, String message) {
        return NotionSyncProgress.builder()
                .step(step)
                .count(count)
                .message(message)
                .build();
    }
    
    public static NotionSyncProgress ofPageBlock (NotionSyncStep step, SyncCount count, String currentPageKey, String message) {
        return NotionSyncProgress.builder()
                .step(step)
                .count(count)
                .currentPageKey(currentPageKey)
                .message(message)
                .build();
    }
}
