package com.team.catchup.rag.dto.internal;

import java.time.LocalDateTime;

public record CommitInfo(
        String message,
        String author,
        LocalDateTime date
) {
}
