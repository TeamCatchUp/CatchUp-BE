package com.team.catchup.common.sse.service;

import com.team.catchup.common.sse.dto.SseEventType;
import com.team.catchup.common.sse.dto.SyncSseMessage;
import com.team.catchup.common.sse.dto.MessageType;
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
public class SyncNotificationService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, Object> userLocks = new ConcurrentHashMap<>();

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter subscribe(Long userId) {
        Object userLock = userLocks.computeIfAbsent(userId, k -> new Object());

        synchronized (userLock) {
            // 기존 연결 정리
            SseEmitter existingEmitter = emitters.get(userId);
            if(existingEmitter != null) {
                log.warn("[SSE] Existing Connection found for userId : {}", userId);
                try {
                    existingEmitter.complete();
                } catch (Exception e) {
                    log.debug("[SSE] Error closing existing Emitter userId : {}", userId);
                }
            }

            // 새 Emitter 생성 (락 내부에서!)
            SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
            emitters.put(userId, emitter);

            // 콜백 등록
            emitter.onCompletion(() -> handleEmitterCompletion(userId));
            emitter.onTimeout(() -> handleEmitterTimeout(userId));
            emitter.onError((e) -> handleEmitterError(userId, e));

            // 연결 메시지 전송 (락 내부에서!)
            SyncSseMessage<Void> connectMessage = SyncSseMessage.simple(
                    MessageType.MESSAGE,
                    SseEventType.CONNECT,
                    "Successfully connected - userId: " + userId
            );

            try {
                emitter.send(SseEmitter.event()
                        .name(connectMessage.type().name())
                        .data(connectMessage));
                log.debug("[SSE] Connection message sent - userId: {}", userId);
            } catch (IOException e) {
                log.error("[SSE] Failed to send connection message - userId: {}", userId, e);
                emitters.remove(userId);
                cleanupUserLock(userId);
                throw new RuntimeException("Failed to establish SSE connection", e);
            }

            return emitter;
        }
    }

    public void sendToClient(Long userId, SyncSseMessage<?> message) {
        Object userLock = userLocks.get(userId);

        if (userLock == null) {
            log.warn("[SSE] No active session for userId: {}", userId);
            return;
        }

        synchronized (userLock) {
            SseEmitter emitter = emitters.get(userId);

            if (emitter == null) {
                log.warn("[SSE] User not found - userId: {}", userId);
                cleanupUserLock(userId);
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
                    cleanupUserLock(userId);
                    log.info("[SSE] Connection closed - userId: {}, type: {}", userId, message.type());
                }

            } catch (IOException e) {
                log.error("[SSE] Failed to send message - userId: {}, type: {}",
                        userId, message.type(), e);
                emitters.remove(userId);
                cleanupUserLock(userId);
            }
        }
    }

    private void handleEmitterCompletion(Long userId) {
        log.info("[SSE] Connection Completed - userId: {} ", userId);

        Object userLock = userLocks.get(userId);
        if(userLock != null) {
            synchronized (userLock) {
                emitters.remove(userId);
                cleanupUserLock(userId);
            }
        }
    }

    private void handleEmitterTimeout(Long userId) {
        log.info("[SSE] Connection Timeout - userId: {} ", userId);

        Object userLock = userLocks.get(userId);
        if(userLock != null) {
            synchronized (userLock) {
                SseEmitter emitter = emitters.remove(userId);
                if(emitter != null) {
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        log.debug("[SSE] Error completing Emitter on Timeout - userId : {}", userId, e);
                    }
                }
                cleanupUserLock(userId);
            }
        }
    }

    private void handleEmitterError(Long userId, Throwable e) {
        log.info("[SSE] Connection Error - userId: {}", userId, e);

        Object userLock = userLocks.get(userId);
        if(userLock != null) {
            synchronized (userLock) {
                emitters.remove(userId);
                cleanupUserLock(userId);
            }
        }
    }

    private void cleanupUserLock(Long userId) {
        if(!emitters.containsKey(userId)) {
            userLocks.remove(userId);
            log.debug("[SSE] Cleaned Up User Lock - userId: {}", userId);
        }
    }
}