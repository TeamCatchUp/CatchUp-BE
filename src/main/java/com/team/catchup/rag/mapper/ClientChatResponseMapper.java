package com.team.catchup.rag.mapper;

import com.team.catchup.rag.dto.client.ClientChatResponse;
import com.team.catchup.rag.dto.client.ClientSource;
import com.team.catchup.rag.dto.internal.CommitInfo;
import com.team.catchup.rag.dto.server.ServerChatResponse;
import com.team.catchup.rag.dto.server.ServerCodeSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClientChatResponseMapper {

    private final ClientSourceMapper sourceMapper;

    public ClientChatResponse map(
            UUID sessionId,
            ServerChatResponse serverResponse,
            Map<String, CommitInfo> commitInfoMap,
            Long chatHistoryId,
            Boolean hasFeedback
    ) {
        List<ClientSource> clientSources = serverResponse.sources().stream()
                .map(source -> {
                        CommitInfo commitInfo = null;
                        if (source instanceof ServerCodeSource serverCodeSource) {
                            commitInfo = commitInfoMap.get(serverCodeSource.getFilePath());
                        }
                    return sourceMapper.map(source, commitInfo);
                })
                .toList();

        return ClientChatResponse.createFinalResponse(sessionId, serverResponse.answer(), clientSources, chatHistoryId, hasFeedback);
    }
}
