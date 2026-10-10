package com.dappunq.service;

import com.dappunq.dto.whoscored.PlayerStatisticsResponse;
import com.dappunq.persistence.NewLeagueEntity;
import com.dappunq.persistence.TeamEntity;
import com.dappunq.persistence.WhoScoredEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WhoScoredScrapingService {

    public WhoScoredScrapingService() {
    }

    public void scrape() {
        String URL = "https://www.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=" +
            "0&isCurrent=true&playerId=&teamIds=&matchId=&stageId=&tournamentOptions=2,3,4,5,22&sortBy=Rating&sortAscending=&age=" +
            "&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=" +
            "&timeOfTheGameStart=&isMinApp=true&page=&includeZeroValues=&numberOfPlayersToPick=10&incPens=";

        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(true);
            Browser browser = playwright.chromium().launch(options);

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"));

            Page page = context.newPage();
            page.navigate(URL);

            ObjectMapper mapper = new ObjectMapper();
            String json = page.textContent("body");
            PlayerStatisticsResponse data;
            try {
                data = mapper.readValue(json, PlayerStatisticsResponse.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            Map<Long, NewLeagueEntity> leagueEntities = new HashMap<>();
            List<WhoScoredEntity> playerEntities = new ArrayList<>();
            Map<Long, TeamEntity> teamEntities = new HashMap<>();

            data.playerTableStats().forEach(
                player -> {
                    TeamEntity teamEntity = teamEntities.computeIfAbsent(player.teamId(), id -> new TeamEntity(id, player.teamName()));
                    NewLeagueEntity leagueEntity = leagueEntities.computeIfAbsent(player.tournamentId(), id -> new NewLeagueEntity(id, player.tournamentName(), player.tournamentRegionName()));
                    WhoScoredEntity whoScoredEntity = new WhoScoredEntity(
                        player.playerId(),
                        player.name(),
                        teamEntity,
                        leagueEntity,
                        player.age(),
                        player.height(),
                        player.weight(),
                        player.positionText(),
                        player.apps(),
                        player.minsPlayed(),
                        player.goal(),
                        player.assistTotal(),
                        Math.round(player.yellowCard()),
                        Math.round(player.redCard()),
                        (float) (int) (player.shotsPerGame() * 10) / 10f,
                        (float) (int) (player.passSuccess() * 10) / 10f,
                        (float) (int) (player.aerialWonPerGame() * 10) / 10f,
                        player.manOfTheMatch()
                    );
                    playerEntities.add(whoScoredEntity);
                }
            );
            // TODO: Save all leagueEntities in leagueEntities
            // TODO: Save all teams in teamEntities
            // TODO: Save all players in playerEntities
        }

    }

}
