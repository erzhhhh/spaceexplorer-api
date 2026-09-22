package com.erzhena.spaceexplorer_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// внутри меня инструкции: как создать другие бины и что включить
@Configuration
// По умолчанию Spring не ищет методы с @Scheduled. Аннотация @EnableScheduling
// включает эту функцию: при старте Spring находит все такие методы и запускает для них таймеры.
@EnableScheduling
public class SchedulingConfig {
}