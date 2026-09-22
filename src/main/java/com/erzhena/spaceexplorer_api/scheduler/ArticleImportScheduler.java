package com.erzhena.spaceexplorer_api.scheduler;

import com.erzhena.spaceexplorer_api.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component // я сам бин, создай меня. Spring запускает schedule только в своих бинах.
public class ArticleImportScheduler {

    private static final Logger log = LoggerFactory.getLogger(ArticleImportScheduler.class);

    private final ArticleService service;

    public ArticleImportScheduler(ArticleService service) {
        this.service = service;
    }

    // Spring сам вызывает этот метод по таймеру через 15 минут после окончания предыдущего запуска
    @Scheduled(fixedDelayString = "PT15M")
    public void importArticles() {
        int saved = service.importFromSnapi(20);
        log.info("Scheduled import finished: {} articles saved", saved);
    }
}