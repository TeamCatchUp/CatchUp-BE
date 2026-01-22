package com.team.catchup.rag.mapper;

import com.team.catchup.rag.dto.client.ChatHistoryResponse;
import com.team.catchup.rag.dto.client.ClientSource;
import com.team.catchup.rag.dto.internal.CommitInfo;
import com.team.catchup.rag.dto.server.ServerCodeSource;
import com.team.catchup.rag.entity.ChatHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatHistoryMapper {
    private final ClientSourceMapper clientSourceMapper;

    public ChatHistoryResponse map(ChatHistory history, Map<String, CommitInfo> commitInfoMap) {
        List<ClientSource> clientSources = Collections.emptyList();

        if (history.getMetadata() != null && history.getMetadata().serverSources() != null) {
            clientSources = history.getMetadata().serverSources().stream()
                    .map(source -> {
                        CommitInfo info = null;
                        if (source instanceof ServerCodeSource codeSource) {
                            info = commitInfoMap.get(codeSource.getFilePath());
                        }

                        return clientSourceMapper.map(source, info);
                    })
                    .toList();
        }

        return ChatHistoryResponse.builder()
                .id(history.getId())
                .role(history.getRole())
                .content(history.getContent())
                .sources(clientSources)
                .createdAt(history.getCreatedAt())
                .build();
    }
}
