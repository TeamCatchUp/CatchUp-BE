package com.team.catchup.meilisearch.document;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * MeiliSearchDocument 인터페이스
 * 구현체는 해당 Document가 저장될 index 이름을 반환하는 메서드를 반드시 구현해야 함
 */
//  Json 내부의 "documentType" 필드를 토대로 구현체를 특정함으로써 직렬화 수헹
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "documentType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JiraIssueDocument.class, name = "jiraIssue")
        /* TODO: 추후에 ConfluenceDocument, NotionDocument 등 추가 등록 */
})
public interface MeiliSearchDocument {

    /* 이 문서가 저장될 저장될 MeiliSearch 인덱스 이름 반환. */
    String getIndexName();

    /* 이 문서의 MeiliSearch Primary Key 필드 이름 반환. */
    String getPrimaryKeyFieldName();
}
