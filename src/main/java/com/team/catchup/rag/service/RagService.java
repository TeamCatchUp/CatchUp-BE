package com.team.catchup.rag.service;

import com.team.catchup.member.entity.Member;
import com.team.catchup.member.repository.MemberRepository;
import com.team.catchup.rag.client.RagApiClient;
import com.team.catchup.rag.dto.server.ServerChatRequest;
import com.team.catchup.rag.dto.server.ServerChatResponse;
import com.team.catchup.rag.dto.client.ClientChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final MemberRepository memberRepository;
    private final ChatUsageLimitService chatUsageLimitService;
    private final ChatHistoryService chatHistoryService;
    private final RagApiClient ragApiClient;

    /**
     * 일일 채팅 제한에 도달했다면 즉시 응답을 반환하고, 그렇지 않으면 RAG 채팅 요청을 처리한다.
     */
    public Mono<ClientChatResponse> requestChat(String query, UUID sessionId, Long memberId, List<String> indexList) {
        return checkUsageLimit(memberId, sessionId)
                .flatMap(optionalResponse ->
                        Mono.justOrEmpty(optionalResponse)
                )
                .switchIfEmpty(Mono.defer(() ->
                        processChatRequest(memberId, sessionId, query, indexList)
                ));
    }

    /**
     * 일일 채팅 제한에 도달했는지 여부를 확인한다.
     */
    private Mono<Optional<ClientChatResponse>> checkUsageLimit(Long memberId, UUID sessionID) {
        return Mono.fromCallable(() ->
                Optional.ofNullable(chatUsageLimitService.checkAndIncrementUsageLimit(memberId, sessionID))
        ).subscribeOn(Schedulers.boundedElastic());
    }


    /**
     * RAG 서버로 채팅 요청을 보내며, 전후로 사용자 쿼리와 어시스턴트 응답을 DB에 저장한다.
     */
    private Mono<ClientChatResponse> processChatRequest(Long memberId, UUID sessionId, String query, List<String> indexList) {
        return findMember(memberId)
                .flatMap(member ->
                        chatHistoryService.saveUserQuery(member, sessionId, query, indexList)
                                .then(ragApiClient.requestChat(ServerChatRequest.of(query, null, sessionId, indexList))
                                        .onErrorResume(e -> {
                                            log.info("RAG 서버 오류: {}", e.getMessage());
                                            return Mono.just(ServerChatResponse.createError("죄송합니다. 답변을 생성하지 못했습니다."));
                                        }))
                                .flatMap(serverRes ->
                                        chatHistoryService.saveAssistantResponse(member, sessionId, serverRes)
                                                .thenReturn(serverRes)
                                )
                                .map(serverRes -> ClientChatResponse.from(sessionId, serverRes))
                );
    }

    /**
     * memberId 기준으로 Mono로 감싼 Member객체를 반환하는 헬퍼 함수
     */
    private Mono<Member> findMember(Long memberId) {
        return Mono.fromCallable(() -> memberRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 이용자입니다.")))
                .subscribeOn(Schedulers.boundedElastic());
    }
}