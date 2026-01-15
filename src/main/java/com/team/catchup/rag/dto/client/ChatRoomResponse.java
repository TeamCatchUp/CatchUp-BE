package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.entity.ChatRoom;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomResponse(
        UUID sessionId,
        String title,
        LocalDateTime lastActiveTime
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getSessionId(),
                chatRoom.getTitle(),
                chatRoom.getLastActiveTime()
        );
    }
}
