package com.erzhena.spaceexplorer_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration // @Module
public class SnapiConfig {

    @Bean // @Provides
    public RestClient snapiRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));


        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        JdkClientHttpRequestFactory factory2 = new JdkClientHttpRequestFactory(httpClient);
        factory2.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl("https://api.spaceflightnewsapi.net/v4")
                .requestFactory(factory)
                .build();
    }
}
