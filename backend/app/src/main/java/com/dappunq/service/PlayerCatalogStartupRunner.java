package com.dappunq.service;

import com.dappunq.config.LeagueCatalogProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PlayerCatalogStartupRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerCatalogStartupRunner.class);
    private final PlayerCatalogRefreshService refreshService;

    public PlayerCatalogStartupRunner(PlayerCatalogRefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @Override
    public void run(ApplicationArguments args) {
        LeagueCatalogProperties.leagues().forEach(league -> {
            try {
                refreshService.refreshLeague(league);
            } catch (RuntimeException exception) {
                LOGGER.warn("No se pudo actualizar la liga {}", league.code(), exception);
            }
        });
    }
}
