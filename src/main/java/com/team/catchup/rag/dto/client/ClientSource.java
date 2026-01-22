package com.team.catchup.rag.dto.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientSource {

    private Integer index;

    private Boolean isCited;

    private Integer sourceType;

    private Double relevanceScore;

    private String htmlUrl;

    private String content;

    private String owner;

    private String repo;

}
