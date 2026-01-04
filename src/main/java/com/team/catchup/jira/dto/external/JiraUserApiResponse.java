package com.team.catchup.jira.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraUserApiResponse(
        @JsonProperty("accountId")
        String accountId,

        @JsonProperty("accountType")
        String accountType,

        @JsonProperty("displayName")
        String displayName,

        @JsonProperty("avatarUrls")
        AvatarUrls avatarUrls,

        @JsonProperty("active")
        Boolean active,

        @JsonProperty("locale")
        String locale,

        @JsonProperty("timeZone")
        String timeZone,

        @JsonProperty("self")
        String self
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AvatarUrls(
            @JsonProperty("48x48")
            String avatarUrl
    ) {}
}