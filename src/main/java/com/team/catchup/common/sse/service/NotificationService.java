package com.team.catchup.common.sse.service;

import com.team.catchup.common.sse.dto.SseEventType;
import com.team.catchup.common.sse.dto.SseMessage;
import com.team.catchup.common.sse.dto.SyncTarget;
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
            log.info("[SSE] Connection completed - userId: {}", userId);
            emitters.remove(userId);
        });

        emitter.onTimeout(() -> {
            log.info("[SSE] Connection timeout - userId: {}", userId);
            emitters.remove(userId);
        });

        emitter.onError((e) -> {
            log.error("[SSE] Connection error - userId: {}", userId, e);
            emitters.remove(userId);
        });

        SseMessage<Void> connectMessage = SseMessage.simple(
                SyncTarget.MESSAGE,
                SseEventType.CONNECT,
                "Successfully connected - userId: " + userId
        );
        sendToClient(userId, connectMessage);

        return emitter;
    }

    public void sendToClient(String userId, SseMessage<?> message) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            log.warn("[SSE] User not found - userId: {}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(message.type().name())
                    .data(message));

            log.debug("[SSE] Message sent - userId: {}, type: {}", userId, message.type());

            // COMPLETED 또는 FAILED 시 연결 종료
            if (message.type() == SseEventType.COMPLETED || message.type() == SseEventType.FAILED) {
                emitter.complete();
                emitters.remove(userId);
                log.info("[SSE] Connection closed - userId: {}, type: {}", userId, message.type());
            }

        } catch (IOException e) {
            log.error("[SSE] Failed to send message - userId: {}, type: {}",
                    userId, message.type(), e);
            emitters.remove(userId);
        }
    }
}