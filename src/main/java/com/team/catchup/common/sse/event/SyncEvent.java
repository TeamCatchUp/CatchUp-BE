package com.team.catchup.common.sse.event;

import com.team.catchup.common.sse.dto.SseEventType;
import com.team.catchup.common.sse.dto.SyncTarget;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SyncEvent {
    private final String userId;
    private final SyncTarget target;
    private final SseEventType type;
    private final Object data;
}
