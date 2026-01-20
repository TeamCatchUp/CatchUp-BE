package com.team.catchup.rag.controller;

import com.team.catchup.auth.user.CustomUserDetails;
import com.team.catchup.rag.dto.client.ChatHistoryResponse;
import com.team.catchup.rag.dto.client.ChatRoomResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatHistoryService chatHistoryService;

    @GetMapping("/api/chatrooms")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms(@AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        List<ChatRoomResponse> response = chatRoomService.getChatRooms(userDetails.getMemberId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/chatrooms/messages")
    public ResponseEntity<Slice<ChatHistoryResponse>> getChatHistory(
            @RequestParam UUID sessionId,
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

}
