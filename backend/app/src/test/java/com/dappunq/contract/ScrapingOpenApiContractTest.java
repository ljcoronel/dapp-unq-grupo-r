package com.dappunq.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapingOpenApiContractTest {
    private static final Path CONTRACT = Path.of("src", "main", "resources", "static", "openapi.yaml");

    @Test
    void scrapeIsPublicAndRecalculateRemainsProtected() throws Exception {
        String openApi = Files.readString(CONTRACT);

        assertThat(operation(openApi, "/quotes/scrape"))
                .contains("security: []")
                .doesNotContain("ApiKeyAuth")
                .contains("'400':");
        assertThat(operation(openApi, "/quotes/recalculate"))
                .contains("security:", "ApiKeyAuth: []");
        assertThat(openApi).contains("ApiKeyAuth:");
    }

    private String operation(String openApi, String path) {
        int start = openApi.indexOf("  " + path + ":");
        assertThat(start).isGreaterThanOrEqualTo(0);
        int end = openApi.indexOf("\n  /", start + 1);
        int components = openApi.indexOf("\ncomponents:", start + 1);
        if (end < 0 || (components >= 0 && components < end)) {
            end = components;
        }
        return openApi.substring(start, end >= 0 ? end : openApi.length());
    }
}
