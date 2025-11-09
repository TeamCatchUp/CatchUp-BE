package com.team.catchup.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MeiliSearch 설정
 */
@Configuration
public class MeiliSearchConfig {

    @Value("${meilisearch.url}")
    private String meiliSearchUrl;

    @Value("${meilisearch.api-key}")
    private String meiliSearchApiKey;

    @Bean
    public Client meiliSearchClient(){
        Config config = new Config(meiliSearchUrl, meiliSearchApiKey);
        return new Client(config);
    }
}
