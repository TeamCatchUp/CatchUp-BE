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

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, Object> userLocks = new ConcurrentHashMap<>();

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter subscribe(Long userId) {
        Object userLock = userLocks.computeIfAbsent(userId, k -> new Object());

        synchronized (userLock) {
            SseEmitter existingEmitter = emitters.get(userId);
            if(existingEmitter != null) {
                log.warn("[SSE] Existing Connection found for userId {}", userId);
                try{
                    existingEmitter.complete();
                } catch (Exception e) {
                    log.debug("[SSE] Error closing existing Emitter", e);
                }
            }

            SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
            emitters.put(userId, emitter);

            emitter.onCompletion(() -> handleEmitterCompletion(userId));
            emitter.onTimeout(() -> handleEmitterTimeout(userId));
            emitter.onError((e) -> handleEmitterError(userId, e));

            SseMessage<Void> connectMessage = SseMessage.simple(
                    SyncTarget.MESSAGE,
                    SseEventType.CONNECT,
                    "[SSE] Successfully connected - userId " + userId
            );

            sendToClientInternal(userId, emitter, connectMessage);
            return emitter;
        }
    }

    public void sendToClient(Long userId, SseMessage<?> message) {
        Object userLock = userLocks.get(userId);
        if(userLock == null) {
            log.warn("[SSE] No active Connection for userId - {}", userId);
            return;
        }

        synchronized (userLock) {
            SseEmitter emitter = emitters.get(userId);

            if(emitter == null) {
                log.warn("[SSE] User NOT FOUND userId - {}", userId);
                cleanupUserLock(userId);
                return;
            }

            sendToClientInternal(userId,emitter, message);
        }
    }

    private void sendToClientInternal(Long userId, SseEmitter emitter, SseMessage<?> message) {
        try {
            emitter.send(SseEmitter.event()
                    .name(message.type().name())
                    .data(message));

            log.debug("[SSE] Sent message for userId - {}/ type - {}", userId, message.type());

            if(message.type() == SseEventType.COMPLETED
            || message.type() == SseEventType.FAILED
                    || message.type() == SseEventType.RAG_DONE
            || message.type() == SseEventType.RAG_INTERRUPT) {
                emitter.complete();
                emitters.remove(userId);
                cleanupUserLock(userId);
                log.info("[SSE] Connection Closed for userId - {}/ type - {}", userId, message.type());
            }
        } catch (IOException e) {
            log.error("[SSE] Failed to send message - userId: {}, type: {}",
                    userId, message.type(), e);
            emitters.remove(userId);
            cleanupUserLock(userId);
        }
    }

    private void handleEmitterCompletion(Long userId) {
        log.info("[SSE] Connection Completed - userId: {}", userId);
        Object userLock = userLocks.get(userId);
        if(userLock != null) {
            synchronized (userLock) {
                emitters.remove(userId);
                cleanupUserLock(userId);
            }
        }
    }

    private void handleEmitterTimeout(Long userId) {
        log.info("[SSE] Connection Timeout - userId: {}", userId);
        Object userLock = userLocks.get(userId);
        if(userLock != null) {
            synchronized (userLock) {
                SseEmitter emitter = emitters.remove(userId);
                if(emitter != null) {
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("[SSE] Error closing emitter on Timeout", e);
                    }
                }
                cleanupUserLock(userId);
            }
        }
    }

    private void handleEmitterError(Long userId, Throwable e) {
        log.error("[SSE] Connection error - userId: {}", userId, e);
        Object userLock = userLocks.get(userId);
        if (userLock != null) {
            synchronized (userLock) {
                emitters.remove(userId);
                cleanupUserLock(userId);
            }
        }
    }

    private void cleanupUserLock(Long userId) {
        if(!emitters.containsKey(userId)) {
            userLocks.remove(userId);
            log.info("[SSE] User Lock Cleaned - userId: {}", userId);
        }
    }

    /**
     * SSE 연결 여부 확인
     * @param userId 사용자 ID
     * @return 연결되어 있으면 true, 아니면 false
     */
    public boolean hasActiveConnection(Long userId) {
        return emitters.containsKey(userId);
    }
}