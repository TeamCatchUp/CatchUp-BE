package com.team.catchup.rag.service;

import com.team.catchup.member.entity.Member;
import com.team.catchup.member.repository.MemberRepository;
import com.team.catchup.rag.dto.client.ClientChatResponse;
import com.team.catchup.rag.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MemberRepository memberRepository;
    private final ChatUsageLimitService chatUsageLimitService;
    private final ChatHistoryService chatHistoryService;
    private final ChatRoomService chatRoomService;
    private final RagProcessingService ragProcessingService;

    /**
     * 일일 채팅 제한 여부 확인
     */
    public ClientChatResponse checkUsageLimit(Long memberId, UUID sessionId) {
        return chatUsageLimitService.checkAndIncrementUsageLimit(memberId, sessionId);
    }
    
   @Transactional
    public void requestChat(String query, UUID sessionId, List<String> indexList, Long memberId) {
       // 멤버 조회
       Member member = memberRepository.findById(memberId)
               .orElseThrow(() -> new RuntimeException("존재하지 않는 이용자입니다."));

       // 채팅방 조회 또는 생성
       ChatRoom chatRoom = chatRoomService.getOrCreateChatRoom(member, sessionId, query);

       // 사용자 쿼리 저장
       chatHistoryService.saveUserQuery(member, chatRoom, query, indexList);

       // (비동기) FastAPI RAG 서버로 채팅 응답 생성 요청
       ragProcessingService.processRagAsync(member, chatRoom, query, indexList);
   }
}