package com.team.catchup.rag.service;

import com.team.catchup.github.entity.GithubCommit;
import com.team.catchup.github.service.GithubCommitService;
import com.team.catchup.member.entity.Member;
import com.team.catchup.member.repository.MemberRepository;
import com.team.catchup.rag.dto.client.ChatHistoryResponse;
import com.team.catchup.rag.dto.client.UserQueryHistoryResponse;
import com.team.catchup.rag.dto.internal.CommitInfo;
import com.team.catchup.rag.dto.server.ServerChatResponse;
import com.team.catchup.rag.dto.server.ServerCodeSource;
import com.team.catchup.rag.entity.ChatHistory;
import com.team.catchup.rag.entity.ChatRoom;
import com.team.catchup.rag.mapper.ChatHistoryMapper;
import com.team.catchup.rag.repository.ChatFeedbackRepository;
import com.team.catchup.rag.repository.ChatHistoryRepository;
import com.team.catchup.rag.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {
    private final GithubCommitService githubCommitService;

    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatHistoryMapper chatHistoryMapper;
    private final ChatFeedbackRepository chatFeedbackRepository;

    /**
     * 사용자 쿼리와 메타데이터를 DB에 저장한다.
     */
    @Transactional
    public void saveUserQuery(Member member, ChatRoom chatRoom, String query, List<String> indexList) {
        ChatHistory userLog = ChatHistory.createUserInfo(chatRoom, member, query, indexList);
        chatHistoryRepository.save(userLog);
    }

    /**
     * 어시스턴트 응답과 메타데이터를 DB에 저장한다.
     */
    @Transactional
    public ChatHistory saveAssistantResponse(Member member, ChatRoom chatRoom, ServerChatResponse response) {
        ChatHistory assistantLog = ChatHistory.createAssistantInfo(chatRoom, member, response);
        return chatHistoryRepository.save(assistantLog);
    }

    /**
     * 채팅 히스토리 조회
     */
    @Transactional(readOnly = true)
    public Slice<ChatHistoryResponse> getChatHistory(UUID sessionId, Long memberId, Pageable pageable) {
        ChatRoom chatRoom = getChatRoom(sessionId);
        validateMember(chatRoom, memberId);

        Slice<ChatHistory> histories = chatHistoryRepository.findByChatRoom(chatRoom, pageable);
        Set<String> allFilePaths = histories.getContent().stream()
                .filter(h -> h.getMetadata() != null && h.getMetadata().serverSources() != null)
                .flatMap(h -> h.getMetadata().serverSources().stream())
                .filter(s -> s instanceof ServerCodeSource)
                .map(s -> ((ServerCodeSource) s).getFilePath())
                .collect(Collectors.toSet());
        Map<String, CommitInfo> commitInfoMap = getCommitInfosByPaths(allFilePaths);

        return histories.map(history -> chatHistoryMapper.map(history, commitInfoMap));
    }

    /**
     * 특정 채팅방에서 사용자가 남긴 쿼리 목록
     */
    @Transactional(readOnly = true)
    public Slice<UserQueryHistoryResponse> getUserQueryHistories(UUID sessionId, Long memberId, Pageable pageable) {
        ChatRoom chatRoom = getChatRoom(sessionId);
        validateMember(chatRoom, memberId);

        return chatHistoryRepository.findByChatRoomAndRole(chatRoom, "user", pageable)
                .map(history -> {
                    boolean hasFeedback = chatFeedbackRepository.existsByMemberIdAndChatHistoryId(
                            memberId,
                            history.getId()
                    );
                    return UserQueryHistoryResponse.from(history, hasFeedback);
                });
    }

    /**
     * 특정 유저가 남긴 모든 쿼리 목록
     */
    @Transactional(readOnly = true)
    public Slice<UserQueryHistoryResponse> getAllUserQueryHistories(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return chatHistoryRepository.findByMemberAndRole(member, "user", pageable)
                .map(history -> {
                    boolean hasFeedback = chatFeedbackRepository.existsByMemberIdAndChatHistoryId(
                            memberId,
                            history.getId()
                    );
                    return UserQueryHistoryResponse.from(history, hasFeedback);
                });
    }

    /**
     * 파일 경로 목록을 받아 CommitInfo 맵을 생성
     */
    private Map<String, CommitInfo> getCommitInfosByPaths(Set<String> filePaths) {
        Map<String, CommitInfo> map = new HashMap<>();

        for (String path : filePaths) {
            // TODO: 추후 githubCommitService.findAllByFilePathsIn(paths) 형태로 쿼리 최적화 권장
            GithubCommit commit = githubCommitService.getLatestCommit(path);

            if (commit != null) {
                map.put(path, new CommitInfo(
                        commit.getMessage(),
                        commit.getAuthorName(),
                        commit.getAuthorDate()
                ));
            }
        }
        return map;
    }

    /**
     * sessionId에 해당하는 ChatRoom 반환 헬퍼 함수
     */
    private ChatRoom getChatRoom(UUID sessionId) {
        return chatRoomRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 없습니다."));
    }

    /**
     * ChatRoom에 대한 Member의 접근 권한 확인
     */
    private void validateMember(ChatRoom chatRoom, Long memberId) {
        if (!chatRoom.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("해당 채팅방에 접근 권한이 없습니다.");
        }
    }
}
