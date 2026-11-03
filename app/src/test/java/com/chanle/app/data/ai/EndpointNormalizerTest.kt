package com.chanle.app.data.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class EndpointNormalizerTest {

    @Test
    fun `adds scheme and chat completions suffix`() {
        assertEquals(
            "https://api.openai.com/v1/chat/completions",
            EndpointNormalizer.normalize("api.openai.com/v1"),
        )
    }

    @Test
    fun `keeps existing chat completions path`() {
        assertEquals(
            "https://api.deepseek.com/chat/completions",
            EndpointNormalizer.normalize("https://api.deepseek.com/chat/completions"),
        )
    }

    @Test
    fun `removes trailing slash`() {
        assertEquals(
            "https://api.deepseek.com/chat/completions",
            EndpointNormalizer.normalize("https://api.deepseek.com/"),
        )
    }

    @Test
    fun `upgrades http to https`() {
        assertEquals(
            "https://example.com/v1/chat/completions",
            EndpointNormalizer.normalize("http://example.com/v1/chat/completions"),
        )
    }

    @Test
    fun `removes surrounding and inner whitespace`() {
        assertEquals(
            "https://api.openai.com/v1/chat/completions",
            EndpointNormalizer.normalize("  api . openai.com /v1  "),
        )
    }

    @Test
    fun `empty input returns empty`() {
        assertEquals("", EndpointNormalizer.normalize("   "))
    }

    @Test
    fun `display strips scheme`() {
        assertEquals(
            "api.openai.com/v1/chat/completions",
            EndpointNormalizer.display("https://api.openai.com/v1/chat/completions"),
        )
    }
}
