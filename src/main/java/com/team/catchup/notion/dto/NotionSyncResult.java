package com.team.catchup.notion.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotionSyncResult {

    private final NotionSyncCount userMetaData;
    private final NotionSyncCount pageMetaData;

    private final boolean success;
    private final String errorMessage;

    public static NotionSyncResult success(NotionSyncCount pageMetaData) {
        return NotionSyncResult.builder()
                .success(true)
                .pageMetaData(pageMetaData)
                .build();
    }

    public static NotionSyncResult failure(String errorMessage) {
        return NotionSyncResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    @Getter
    @Builder
    public static class NotionSyncCount {
        private final int totalFetched;
        private final int saved;
        private final int skipped;

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
}