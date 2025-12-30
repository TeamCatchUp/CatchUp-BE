package com.team.catchup.notion.dto.response;

public record NotionSyncCount(
        int totalFetched,
        int saved,
        int skipped
) {
    public static NotionSyncCount of(int totalFetched, int saved) {
        return new NotionSyncCount(totalFetched, saved, totalFetched - saved);
    }

    public static NotionSyncCount empty() {
        return new NotionSyncCount(0, 0, 0);
    }
}