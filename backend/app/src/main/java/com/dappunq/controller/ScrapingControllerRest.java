package com.dappunq.controller;

import com.dappunq.service.WhoScoredScrapingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScrapingControllerRest {

    private final WhoScoredScrapingService scrapingService;

    public ScrapingControllerRest(WhoScoredScrapingService service) {
        this.scrapingService = service;
    }

    @PostMapping("/scrape")
    public void scrape(@RequestParam(defaultValue = "1000") int numberOfPlayers) {
        scrapingService.scrape(String.valueOf(numberOfPlayers));
    }
}
