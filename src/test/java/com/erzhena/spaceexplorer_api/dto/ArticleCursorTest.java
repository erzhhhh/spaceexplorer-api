package com.erzhena.spaceexplorer_api.dto;

import com.erzhena.spaceexplorer_api.exception.InvalidCursorException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ArticleCursorTest {

    @Test
    void decodeReturnsOriginalValues() {
        ArticleCursor original = new ArticleCursor(
                Instant.parse("2026-08-26T19:14:15Z"), 39694L);

        String encoded = original.encode();
        ArticleCursor decoded = ArticleCursor.decode(encoded);

        assertThat(decoded).isEqualTo(original);
    }

    @Test
    void decodeThrowsOnMalformedCursor() {
        assertThatThrownBy(() -> ArticleCursor.decode("hello"))
                .isInstanceOf(InvalidCursorException.class);
    }

    @Test
    void decodeThrowsOnMissingSeparator() {
        String noUnderscore = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("abcdef".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> ArticleCursor.decode(noUnderscore))
                .isInstanceOf(InvalidCursorException.class);
    }

    @Test
    void decodeThrowsOnInvalidTimestamp() {
        String badTime = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("abc_123".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> ArticleCursor.decode(badTime))
                .isInstanceOf(InvalidCursorException.class);
    }

    @Test
    void decodePreservesFractionalSeconds() {
        ArticleCursor original = new ArticleCursor(
                Instant.parse("2026-08-26T19:14:15.123456Z"), 39694L);

        assertThat(ArticleCursor.decode(original.encode())).isEqualTo(original);
    }
}