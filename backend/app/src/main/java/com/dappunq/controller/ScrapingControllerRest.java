package com.dappunq.controller;

import com.dappunq.service.WhoScoredScrapingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScrapingControllerRest {

    private final WhoScoredScrapingService scrapingService;

    public ScrapingControllerRest(WhoScoredScrapingService service) {
        this.scrapingService = service;
    }

    @PostMapping("/scrape")
    public void scrape() {
        scrapingService.scrape();
    }
}
