package com.erzhena.spaceexplorer_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration // Это как @Module
public class SnapiConfig {

    @Bean
    public RestClient snapiRestClient(RestClient.Builder builder,
                                      @Value("${snapi.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
