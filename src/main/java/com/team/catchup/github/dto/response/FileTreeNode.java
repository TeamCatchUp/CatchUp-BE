package com.team.catchup.github.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class FileTreeNode {
    private String name;
    private String path;
    private String type;
    @Builder.Default
    private List<FileTreeNode> children = new ArrayList<>();

    public void addChild(FileTreeNode child) {
        this.children.add(child);
    }
}