package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.catchup.rag.dto.server.ServerCodeSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class ClientCodeSource extends ClientSource{

    @JsonProperty("file_path")
    private String filePath;

    private String category;

    private String lanugage;

    public static ClientCodeSource from(ServerCodeSource source) {
        return ClientCodeSource.builder()
                // 공통 필드
                .index(source.getIndex())
                .isCited(source.getIsCited())
                .sourceType(source.getSourceType())
                .relevanceScore(source.getRelevanceScore())
                .htmlUrl(source.getHtmlUrl())
                .content(source.getText())
                // Code 전용
                .filePath(source.getFilePath())
                .category(source.getCategory())
                .lanugage(source.getLanguage())
                .build();
    }
}
