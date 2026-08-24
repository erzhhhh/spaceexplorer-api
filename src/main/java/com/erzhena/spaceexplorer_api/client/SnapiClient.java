package com.erzhena.spaceexplorer_api.client;

import com.erzhena.spaceexplorer_api.client.dto.SnapiArticle;
import com.erzhena.spaceexplorer_api.client.dto.SnapiArticlesResponse;
import com.erzhena.spaceexplorer_api.exception.SnapiUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component // @Inject constructor --- это бин
public class SnapiClient {

    private final RestClient restClient;

    public SnapiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<SnapiArticle> fetchArticles(int limit) {
        try {
            SnapiArticlesResponse response = restClient.get()
                    .uri("/articles/?limit={limit}", limit)
                    .retrieve()
                    .body(SnapiArticlesResponse.class);
            return response == null ? List.of() : response.results();
        } catch (RestClientException e) { // RestClientException — общий предок всех ошибок RestClient: сеть недоступна,
            // таймаут, кривой ответ, статус 500 от чужого сервера.
            throw new SnapiUnavailableException("Failed to fetch articles from SNAPI", e);
        }
    }
}
