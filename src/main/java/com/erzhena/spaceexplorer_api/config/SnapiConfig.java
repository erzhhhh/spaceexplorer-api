package com.erzhena.spaceexplorer_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration // Это как @Module. Внутри меня инструкции: как создать другие бины и что включить
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SnapiConfig {

    @Bean
    public RestClient snapiRestClient(RestClient.Builder builder,
                                      @Value("${snapi.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
