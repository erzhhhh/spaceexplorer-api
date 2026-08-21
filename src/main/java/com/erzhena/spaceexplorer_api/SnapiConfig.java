package com.erzhena.spaceexplorer_api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration // @Module
public class SnapiConfig {

    @Bean // @Provides
    public RestClient snapiRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.spaceflightnewsapi.net/v4")
                .build();
    }
}
