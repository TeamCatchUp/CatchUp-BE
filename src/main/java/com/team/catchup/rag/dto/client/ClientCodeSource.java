package com.team.catchup.rag.dto.client;

import com.team.catchup.rag.dto.server.ServerCodeSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class ClientCodeSource extends ClientSource{

    private String filePath;

    private String category;

    private String language;

    private Integer daysAgo;

    private String author;

    public static ClientCodeSource of(
            ServerCodeSource source,
            String latestCommitMessage,  // 해당 파일에 대한 최신 커밋
            String author,  // 최신 커밋 작성자
            Integer daysAgo  // 커밋 생성일로부터 지난 날 수
    ) {
        return ClientCodeSource.builder()
                // 공통 필드
                .index(source.getIndex())
                .isCited(source.getIsCited())
                .sourceType(source.getSourceType())
                .relevanceScore(source.getRelevanceScore())
                .htmlUrl(source.getHtmlUrl())
                .content(latestCommitMessage)
                .owner(source.getOwner())
                .repo(source.getRepo())
                // Code 전용
                .filePath(source.getFilePath())
                .category(source.getCategory())
                .language(source.getLanguage())
                .daysAgo(daysAgo)
                .author(author)
                .build();
    }
}
