package com.team.catchup.rag.controller;

import com.team.catchup.auth.user.CustomUserDetails;
import com.team.catchup.rag.dto.client.ClientChatRequest;
import com.team.catchup.rag.dto.client.ClientChatResponse;
import com.team.catchup.rag.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RagController {
    private final RagService ragService;

    @PostMapping("/api/chat")
    public Mono<ResponseEntity<ClientChatResponse>> getChatResponse(
            @Valid @RequestBody ClientChatRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        return ragService.requestChat(request.query(), request.sessionId(), userDetails.getMemberId(), request.indexList())
                .map(ResponseEntity::ok);
    }
}
