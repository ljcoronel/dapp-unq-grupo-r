package com.dappunq.service;

import com.dappunq.dto.footballdata.FootballDataResponse;
import com.dappunq.exception.ExternalIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Service
public class FootballDataClient {
    private final RestClient restClient;

    public FootballDataClient(
            @Value("${football-data.base-url}") String baseUrl,
            @Value("${football-data.auth-token:}") String authToken,
            @Value("${football-data.connect-timeout:2s}") Duration connectTimeout,
            @Value("${football-data.read-timeout:10s}") Duration readTimeout) {
        if (baseUrl == null || !baseUrl.startsWith("https://")) {
            throw new IllegalArgumentException("La URL de football-data debe utilizar HTTPS");
        }
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Auth-Token", authToken)
                .build();
    }

    public FootballDataResponse fetchScorers(String leagueCode) {
        try {
            FootballDataResponse response = restClient.get()
                    .uri("/competitions/{code}/scorers", leagueCode)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(),
                            (request, responseStatus) -> {
                                throw new ExternalIntegrationException(
                                        "football-data respondió con HTTP "
                                                + responseStatus.getStatusCode().value());
                            })
                    .body(FootballDataResponse.class);
            if (response == null) {
                throw new ExternalIntegrationException("football-data devolvió una respuesta vacía");
            }
            return response;
        } catch (ExternalIntegrationException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ExternalIntegrationException("No fue posible consultar football-data", exception);
        }
    }
}
