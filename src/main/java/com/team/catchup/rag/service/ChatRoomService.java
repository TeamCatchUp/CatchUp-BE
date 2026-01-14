package com.team.catchup.rag.service;

import com.team.catchup.member.entity.Member;
import com.team.catchup.rag.entity.ChatRoom;
import com.team.catchup.rag.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 채팅방 조회 또는 생성
     */
    public Mono<ChatRoom> getOrCreateChatRoom(Member member, UUID sessionId, String initialQuery) {
        return Mono.fromCallable(() -> {
            return chatRoomRepository.findById(sessionId)
                    .orElseGet(() -> createChatRoom(member, sessionId, initialQuery));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 채팅방 생성
     */
    private ChatRoom createChatRoom(Member member, UUID sessionId, String initialQuery) {
        String title = initialQuery.length() > 20 ? initialQuery.substring(0, 20) + "..." : initialQuery;
        ChatRoom chatRoom = ChatRoom.builder()
                .sessionId(sessionId)
                .member(member)
                .title(title)
                .build();

        return chatRoomRepository.save(chatRoom);
    }

    /**
     * 마지막 활동 시간 갱신
     */
    public Mono<Void> updateLastActiveTime(UUID sessionId) {
        return Mono.fromRunnable(() -> {
            chatRoomRepository.findById(sessionId).ifPresent(chatRoom -> {
                chatRoom.updateLastActiveTime();
                chatRoomRepository.save(chatRoom);
            });
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
