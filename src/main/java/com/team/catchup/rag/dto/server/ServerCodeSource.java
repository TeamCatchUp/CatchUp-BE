package com.team.catchup.rag.dto.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class ServerCodeSource extends ServerSource{
    @JsonProperty("file_path")
    private String filePath;

    private String category;

    private String language;
}
