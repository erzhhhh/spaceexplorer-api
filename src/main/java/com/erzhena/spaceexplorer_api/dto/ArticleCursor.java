package com.erzhena.spaceexplorer_api.dto;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public record ArticleCursor(Instant publishedAt, long id) {

    public String encode() {
        String raw = publishedAt.toString() + "_" + id;
        // В компьютере всё хранится байтами — числами от 0 до 255. Но не всякий байт можно безопасно вставить в текст.
        // Часть из них — это управляющие символы, которые вообще не отображаются. Другие имеют специальное значение:
        // например, & в адресе разделяет параметры, а # отрезает всё после себя.
        // Если запихнуть такие байты в URL или в письмо, данные исказятся или потеряются.
        // Base64 решает это так: берёт любые байты и переписывает их безопасными символами. Результат гарантированно проходит
        // куда угодно — в адрес, в заголовок, в JSON, в текстовое письмо.
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static ArticleCursor decode(String value) {
        // getUrlEncoder — вариант, безопасный для URL. Обычный Base64 использует символы + и /, а в адресе они имеют
        // своё значение и всё ломают. URL-вариант заменяет их на - и _.
        // withoutPadding — убирает символы = в конце. Они тоже мешаются в URL и ничего не значат.
        String raw = new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        int sep = raw.indexOf('_');
        return new ArticleCursor(
                Instant.parse(raw.substring(0, sep)),
                Long.parseLong(raw.substring(sep + 1)));
    }
}