package com.erzhena.spaceexplorer_api;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component // @Inject constructor --- это бин
public class SnapiClient {

    private final RestClient restClient;

    public SnapiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<SnapiArticle> fetchArticles(int limit) {
        SnapiArticlesResponse response = restClient.get()
                .uri("/articles/?limit={limit}", limit)
                .retrieve()
                .body(SnapiArticlesResponse.class);

        return response == null ? List.of() : response.results();
    }
}
