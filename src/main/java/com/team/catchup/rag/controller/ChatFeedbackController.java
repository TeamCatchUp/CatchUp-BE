package com.team.catchup.rag.controller;

import com.team.catchup.auth.user.CustomUserDetails;
import com.team.catchup.rag.dto.client.ChatFeedbackRequest;
import com.team.catchup.rag.dto.client.ChatFeedbackResponse;
import com.team.catchup.rag.service.ChatFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatFeedbackController {

    private final ChatFeedbackService chatFeedbackService;

    @PostMapping("/api/chat/feedback")
    public ResponseEntity<ChatFeedbackResponse> createFeedback(
            @Valid @RequestBody ChatFeedbackRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ChatFeedbackResponse response = chatFeedbackService.createFeedback(
                userDetails.getMemberId(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
