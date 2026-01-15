package com.team.catchup.rag.controller;

import com.team.catchup.auth.user.CustomUserDetails;
import com.team.catchup.rag.dto.client.ClientChatRequest;
import com.team.catchup.rag.dto.client.ClientChatResponse;
import com.team.catchup.rag.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    /**
     * 채팅 요청 API
     * 답변 생성 과정(예: '검색 중...', '답변 생성 중...')스트리밍 기능을 포함한다.
     * API 호출 전 반드시 SSE 연결 전제된다.
     * 호출 즉시 반환되며 채팅 요청은 내부적으로 비동기적으로 요청/처리되어 SSE 연결을 통해 응답을 전송한다.
     */
    @PostMapping("/api/chat")
    public ResponseEntity<ClientChatResponse> getChatResponse(
            @Valid @RequestBody ClientChatRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        String query = request.query();
        UUID sessionID = request.sessionId();
        List<String> indexList = request.indexList();
        Long memberId = userDetails.getMemberId();

        // 일일 채팅 제한 여부 확인
        ClientChatResponse limitResponse = chatService.checkUsageLimit(memberId, sessionID);
        if (limitResponse != null){
            return ResponseEntity.ok(limitResponse);
        }

        // (비동기) 채팅 요청
        chatService.requestChat(query, sessionID, indexList, memberId);

        return ResponseEntity.ok(ClientChatResponse.of(sessionID, "답변 생성을 시작합니다."));
    }
}
