package com.team.catchup.common.sse.service;

import com.team.catchup.common.sse.dto.SseEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.info("[SSE] Connection Closed");
        });
        emitter.onTimeout(() -> {
            log.info("[SSE] Connection Timeout | userId : {}", userId);
            emitters.remove(userId);
        });
        emitter.onError((e) -> {
            log.error("[SSE] Connection Error | userId : {}", userId, e);
            emitters.remove(userId);
        });

        sendToClient(userId, SseEventType.CONNECT, "Successfully Connected | userId :" + userId);

        return emitter;
    }

    public void sendToClient(String userId, SseEventType type, Object data) {
        SseEmitter emitter = emitters.get(userId);

        if(emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(type.name())
                        .data(data));

                if(type == SseEventType.COMPLETED || type == SseEventType.FAILED) {
                    emitter.complete();
                }
            } catch (IOException e) {
                log.error("[SSE] Failed to Send Event | userId : {}, type: {}", userId, type);
                emitters.remove(userId);
            }
        } else {
            log.warn("[SSE] User NOT FOUND | userId : {}", userId);
        }
    }
}
