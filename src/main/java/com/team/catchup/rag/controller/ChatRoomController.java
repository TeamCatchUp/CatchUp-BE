package com.team.catchup.rag.controller;

import com.team.catchup.auth.user.CustomUserDetails;
import com.team.catchup.rag.dto.client.ChatHistoryResponse;
import com.team.catchup.rag.dto.client.ChatRoomResponse;
import com.team.catchup.rag.dto.client.UserQueryHistoryResponse;
import com.team.catchup.rag.service.ChatHistoryService;
import com.team.catchup.rag.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatHistoryService chatHistoryService;

    @GetMapping("/api/chatrooms")
    public ResponseEntity<Slice<ChatRoomResponse>> getChatRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "lastActiveTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Slice<ChatRoomResponse> response = chatRoomService.getChatRooms(userDetails.getMemberId(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/chatrooms/{sessionId}/messages")
    public ResponseEntity<Slice<ChatHistoryResponse>> getChatHistory(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<ChatHistoryResponse> history = chatHistoryService.getChatHistory(
                sessionId,
                userDetails.getMemberId(),
                pageable
        );
        return ResponseEntity.ok(history);
    }

    /**
     * 특정 채팅방에서 사용자가 남긴 쿼리 목록
     */
    @GetMapping("/api/chatrooms/{sessionId}/queries")
    public ResponseEntity<Slice<UserQueryHistoryResponse>> getUserQueryHistories(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Slice<UserQueryHistoryResponse> queryHistories = chatHistoryService.getUserQueryHistories(
                sessionId,
                userDetails.getMemberId(),
                pageable
        );
        return ResponseEntity.ok(queryHistories);
    }

    /**
     * 특정 유저가 남긴 모든 쿼리 목록
     */
    @GetMapping("/api/chatrooms/queries")
    public ResponseEntity<Slice<UserQueryHistoryResponse>> getAllUserQueryHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<UserQueryHistoryResponse> allQueryHistories = chatHistoryService.getAllUserQueryHistories(
                userDetails.getMemberId(),
                pageable
        );
        return ResponseEntity.ok(allQueryHistories);
    }
}
