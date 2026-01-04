package com.team.catchup.rag.controller;

import com.team.catchup.rag.dto.UserChatRequest;
import com.team.catchup.rag.dto.UserChatResponse;
import com.team.catchup.rag.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RagController {
    private final RagService ragService;

    @PostMapping("/api/chat")
    public Mono<ResponseEntity<UserChatResponse>> getChatResponse(
            @Valid @RequestBody UserChatRequest request,
            Authentication authentication
            ) {
        Long memberId = Long.parseLong(authentication.getName());

        return ragService.requestChat(request.query(), request.sessionId(), memberId)
                .map(ResponseEntity::ok);
    }
}
