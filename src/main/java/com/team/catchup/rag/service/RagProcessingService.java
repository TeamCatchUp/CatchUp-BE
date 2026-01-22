package com.team.catchup.rag.service;

import com.team.catchup.common.sse.dto.SseEventType;
import com.team.catchup.common.sse.dto.SseMessage;
import com.team.catchup.common.sse.dto.SyncTarget;
import com.team.catchup.common.sse.service.NotificationService;
import com.team.catchup.github.entity.GithubCommit;
import com.team.catchup.github.service.GithubCommitService;
import com.team.catchup.member.entity.Member;
import com.team.catchup.rag.client.RagApiClient;
import com.team.catchup.rag.dto.client.ClientChatResponse;
import com.team.catchup.rag.dto.client.ClientChatStreamingFinalResponse;
import com.team.catchup.rag.dto.client.ClientChatStreamingResponse;
import com.team.catchup.rag.dto.client.UserSelectedPullRequest;
import com.team.catchup.rag.dto.internal.CommitInfo;
import com.team.catchup.rag.dto.server.*;
import com.team.catchup.rag.entity.ChatRoom;
import com.team.catchup.rag.mapper.ClientChatResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagProcessingService {

    private final RagApiClient ragApiClient;
    private final NotificationService notificationService;
    private final ChatHistoryService chatHistoryService;
    private final ChatRoomService chatRoomService;

    private final GithubCommitService githubCommitService;
    private final ClientChatResponseMapper clientChatResponseMapper;

    /**
     * (비동기) RAG 답변 생성 과정 스트리밍
     */
    @Async("ragExecutor")
    public void processRagAsync(
            Member member, ChatRoom chatRoom, String query, List<String> indexList
    ) {
        UUID sessionId = chatRoom.getSessionId();
        Long memberId = member.getId();

        try {
            ragApiClient.requestChatStream(ServerChatRequest.of(query, null, sessionId, indexList))
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(fastApiDto -> {
                        // 최종 답변
                        if ("result".equals(fastApiDto.getType())) {
                            handleFinalAnswer(member, chatRoom, fastApiDto);
                        }

                        else if ("interrupt".equals(fastApiDto.getType())) {
                            handleInterrupt(memberId, fastApiDto);
                        }
                        
                        // 중간 과정
                        else {
                            handleProgressLog(memberId, fastApiDto);
                        }
                    })
                    .doOnComplete(() -> {
                        // 최근 활성 시간 갱신
                        chatRoomService.updateLastActiveTime(sessionId);
                        log.info("RAG Stream 완료 - sessionId: {}", sessionId);
                    })
                    .doOnError(e -> {
                        handleError(memberId, sessionId, e);
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("RAG Async 프로세스 시작 실패", e);
            handleError(memberId, sessionId, e);
        }
    }

    /**
     * 중간 과정 스트리밍
     */
    private void handleProgressLog(Long memberId, FastApiStreamingResponse dto) {
        // 답변 생성 과정 정보 추출
        ClientChatStreamingResponse streamingResponse = ClientChatStreamingResponse.from(dto);

        // Client에게 전달할 SseMessage의 data 필드 가공
        SseMessage<ClientChatStreamingResponse> sseMessage = SseMessage.withData(
                SyncTarget.CHAT,
                SseEventType.RAG_IN_PROGRESS,
                streamingResponse
        );

        // Client에게 전송
        notificationService.sendToClient(memberId, sseMessage);
    }

    /**
     * (Human-In-The-Loop) 답변 스트리밍 과정에서 사용자가 개입할 수 있도록
     * Interrupt 이벤트 전송
     */
    private void handleInterrupt(Long memberId, FastApiStreamingResponse dto) {
        ClientChatStreamingResponse interruptResponse = ClientChatStreamingResponse.createInterruptResponse(dto);

        SseMessage<ClientChatStreamingResponse> sseMessage = SseMessage.withData(
                SyncTarget.CHAT,
                SseEventType.RAG_INTERRUPT,
                interruptResponse
        );

        notificationService.sendToClient(memberId, sseMessage);
    }

    /**
     * 최종 응답 스트리밍 및 저장
     */
    private void handleFinalAnswer(Member member, ChatRoom chatRoom, FastApiStreamingResponse dto) {
        
        // 최종 답변 및 출처 추출
        ServerChatResponse serverChatResponse = ServerChatResponse.from(dto);
        
        // 최종 답변 저장
        chatHistoryService.saveAssistantResponse(member, chatRoom, serverChatResponse);

        Map<String, CommitInfo> commitInfoHashMap = new HashMap<>();

        // Client 전달용 LLM 최종 답변 및 출처 가공
        for (ServerSource source : serverChatResponse.sources()) {
            if (source instanceof ServerCodeSource codeSource) {
                String filePath = codeSource.getFilePath();

                // 최신 커밋 조회
                GithubCommit commitEntity = githubCommitService.getLatestCommit(filePath);

                if (commitEntity != null) {
                    CommitInfo info = new CommitInfo(
                            commitEntity.getMessage(),
                            commitEntity.getAuthorName(),
                            commitEntity.getAuthorDate()
                    );
                    commitInfoHashMap.put(filePath, info);
                }
            }
        }

        // Client 전달용 객체 생성
        ClientChatResponse clientChatResponse = clientChatResponseMapper.map(
                chatRoom.getSessionId(),
                serverChatResponse,
                commitInfoHashMap
        );

        // Client에게 전달할 SseMessage의 data 필드 가공
        ClientChatStreamingFinalResponse finalWrapper = ClientChatStreamingFinalResponse.of(
                dto,
                clientChatResponse
        );

        // 최종 SSE 응답 데이터
        SseMessage<ClientChatStreamingFinalResponse> sseMessage = SseMessage.withData(
                SyncTarget.CHAT,
                SseEventType.RAG_DONE,
                finalWrapper
        );
        
        // Client에게 전송
        notificationService.sendToClient(member.getId(), sseMessage);
    }

    /**
     * 답변 생성 과정 중 에러 핸들링
     */
    private void handleError(Long memberId, UUID sessionId, Throwable e) {
        log.error("RAG Streaming 에러 -> sessionId: {}", sessionId, e);
        SseMessage<String> errorMessage = SseMessage.withData(
                SyncTarget.CHAT,
                SseEventType.FAILED,
                "답변 생성 중 오류가 발생했습니다."
        );
        notificationService.sendToClient(memberId, errorMessage);
    }

    @Async("ragExecutor")
    public void resumeRagAsync(
            Member member, ChatRoom chatRoom, UUID sessionId, List<UserSelectedPullRequest> userSelectedPullRequests
    ) {
        Long memberId = member.getId();

        try {
            ragApiClient.resumeChatStream(ServerChatResumeRequest.of(sessionId, userSelectedPullRequests))
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(fastApiDto -> {
                        if ("result".equals(fastApiDto.getType())) {
                            handleFinalAnswer(member, chatRoom, fastApiDto);
                        }

                        else if ("status".equals(fastApiDto.getType())) {
                            handleProgressLog(member.getId(), fastApiDto);
                        }
                    })
                    .doOnComplete(() -> {
                        chatRoomService.updateLastActiveTime(sessionId);
                        log.info("resumRagAsync 완료 - sessionId: {}", sessionId);
                    })
                    .doOnError(e -> {
                        handleError(memberId, sessionId, e);
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("RAG 답변 생성 재개 실패", e);
            handleError(memberId, sessionId, e);
        }
    }
}
