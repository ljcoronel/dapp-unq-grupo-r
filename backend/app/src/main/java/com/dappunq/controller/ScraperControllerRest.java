package com.dappunq.controller;

import com.dappunq.service.ScrapingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScraperControllerRest {

    private final ScrapingService scrapingService;

    public ScraperControllerRest(ScrapingService service) {
        this.scrapingService = service;
    }

    @PostMapping("/scrapeSome")
    public void scrape(@RequestParam(defaultValue = "50") int numberOfPlayers) {
        scrapingService.scrape(String.valueOf(numberOfPlayers));
    }
}
