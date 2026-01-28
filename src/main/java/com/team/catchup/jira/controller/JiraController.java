package com.team.catchup.jira.controller;

import com.team.catchup.auth.user.CustomUserDetails;
import com.team.catchup.jira.dto.request.SaveRecentlyReadIssueRequest;
import com.team.catchup.jira.dto.response.JiraIssueResponse;
import com.team.catchup.jira.service.JiraIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class JiraController {

    private final JiraIssueService jiraIssueService;

    @GetMapping("/api/jira/issues")
    public ResponseEntity<List<JiraIssueResponse>> getRecentlyReadIssues(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<JiraIssueResponse> responses = jiraIssueService.getRecentlyReadIssues(userDetails.getMemberId());

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/api/jira/issues")
    public ResponseEntity<Void> saveRecentlyReadIssue(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SaveRecentlyReadIssueRequest request
    ) {
        jiraIssueService.saveRecentlyReadIssues(userDetails.getMemberId(), request.issueId());

        return ResponseEntity.ok().build();
    }

}