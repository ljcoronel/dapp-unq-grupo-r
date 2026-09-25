package com.dappunq.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FootballDataService {

    private final RestClient restClient;

    public FootballDataService() {
        this.restClient = RestClient.builder()
            .baseUrl("https://api.football-data.org/v4/competitions")
            .defaultHeader("X-Auth-Token", "953db79e406f46c2af7c241e91938d5b")
            .build();
    }


}
