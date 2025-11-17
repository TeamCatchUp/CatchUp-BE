package com.team.catchup.jira.mapper;

import com.team.catchup.jira.dto.response.IssueTypeResponse;
import com.team.catchup.jira.entity.IssueType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IssueTypeMapper {

    public IssueType toEntity(IssueTypeResponse response) {
        try {
            Integer issueTypeId = parseIntegerSafely(response.id());

            // 커스텀 IssueType 확인
            String scopeType = "Global";
            Integer scopeProjectId = null;

            if (response.scope() != null) {
                // scope가 있으면 "Project"
                scopeType = "Project";
                scopeProjectId = parseIntegerSafely(response.scope().project().id());
            }

            return IssueType.builder()
                    .id(issueTypeId)
                    .name(response.name())
                    .iconUrl(response.iconUrl())
                    .isSubtask(response.subtask() != null ? response.subtask() : false)
                    .hierarchyLevel(response.hierarchyLevel())
                    .scopeType(scopeType)
                    .scopeProjectId(scopeProjectId)
                    .build();

        } catch (Exception e) {
            log.error("[Jira] [IssueTypeMapping] 매핑 실패: {}", response.id(), e);
            throw new RuntimeException("IssueType mapping failed: " + response.id(), e);
        }
    }

    private Integer parseIntegerSafely(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("[IssueType] String -> Integer 파싱 실패: {}", value);
            return null;
        }
    }
}