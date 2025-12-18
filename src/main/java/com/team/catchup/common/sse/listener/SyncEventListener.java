package com.team.catchup.common.sse.listener;

import com.team.catchup.common.sse.event.SyncEvent;
import com.team.catchup.common.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncEventListener {

    private final NotificationService notificationService;

    @EventListener
    @Async
    public void handleSyncEvent(SyncEvent event) {
        log.info("[SSE] Event Received | userId :{}, target :{}, type :{}",
                event.getUserId(), event.getTarget(), event.getType());

        notificationService.sendToClient(
                event.getUserId(),
                event.getType(),
                event.getData()
        );
    }
}
