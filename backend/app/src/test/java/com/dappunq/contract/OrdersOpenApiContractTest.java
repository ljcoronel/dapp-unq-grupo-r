package com.dappunq.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OrdersOpenApiContractTest {
    private static final Path CONTRACT = Path.of("..", "specs", "005-swagger", "contracts", "openapi.yaml");

    @Test
    void documentsBearerSecurityAndUnauthorizedResponsesForOrderCreation() throws Exception {
        String openApi = Files.readString(CONTRACT);

        assertThat(openApi).contains("bearerAuth:")
                .contains("scheme: bearer")
                .contains("bearerFormat: JWT");
        assertThat(operation(openApi, "/orders/buy")).contains("security:", "bearerAuth: []", "'401':",
                "CrearOrdenRequest", "OrdenResponse");
        assertThat(operation(openApi, "/orders/sell")).contains("security:", "bearerAuth: []", "'401':",
                "CrearOrdenRequest", "OrdenResponse");
    }

    private String operation(String openApi, String path) {
        int start = openApi.indexOf("  " + path + ":");
        assertThat(start).isGreaterThanOrEqualTo(0);
        int end = openApi.indexOf("\n  /", start + 1);
        return openApi.substring(start, end >= 0 ? end : openApi.length());
    }
}
