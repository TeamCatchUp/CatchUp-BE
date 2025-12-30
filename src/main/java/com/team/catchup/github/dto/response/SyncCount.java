package com.team.catchup.github.dto.response;

public record SyncCount(
    int totalFetched,
    int saved,
    int skipped
) {
    public static SyncCount of(int totalFetched, int saved) {
        return new SyncCount(totalFetched, saved, totalFetched - saved);
    }

    public static SyncCount empty() {
        return new SyncCount(0, 0, 0);
    }
}
