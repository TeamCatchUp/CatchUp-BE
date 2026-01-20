package com.team.catchup.github.service.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerEventMessage implements Serializable {

    /**
     * 이벤트 타입 (PUSH, PULL_REQUEST, ISSUE)
     */
    private EventType eventType;

    /**
     * 리포지토리 정보
     */
    private Long repositoryId;
    private String owner;
    private String repo;

    /**
     * Push 이벤트용 필드
     */
    private String ref;  // 브랜치 정보 (예: refs/heads/main)
    private String branch;  // 브랜치 이름 (예: main, feature/test)
    private List<String> commitShas;  // 새로 추가된 커밋 SHA 목록
    private List<String> changedFilePaths;  // 변경된 파일 경로 목록 (Meilisearch 삭제/재인덱싱용)

    /**
     * PR/Issue용 필드
     */
    private Integer number;
    private String action;  // opened, closed, edited, etc.

    public enum EventType {
        PUSH,
        PULL_REQUEST,
        ISSUE
    }
}
