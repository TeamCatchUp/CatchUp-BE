package com.team.catchup.common.sse.listener;

import com.team.catchup.common.sse.event.SyncEvent;
import com.team.catchup.common.sse.service.SyncNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncEventListener {

    private final SyncNotificationService notificationService;

    @EventListener
    @Async
    public void handleSyncEvent(SyncEvent event) {
        log.info("[SSE EVENT] Received - userId: {}, messageType: {}, type: {}",
                event.getUserId(),
                event.getMessage().messageType(),
                event.getMessage().type());

        notificationService.sendToClient(
                event.getUserId(),
                event.getMessage()
        );
    }
}