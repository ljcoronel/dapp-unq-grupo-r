package com.dappunq.service;

import com.dappunq.dto.whoscored.PlayerStatisticsResponse;
import com.dappunq.persistence.NewLeagueEntity;
import com.dappunq.persistence.NewLeagueRepository;
import com.dappunq.persistence.TeamEntity;
import com.dappunq.persistence.TeamRepository;
import com.dappunq.persistence.WhoScoredEntity;
import com.dappunq.persistence.WhoScoredRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WhoScoredScrapingService {
    private final NewLeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final WhoScoredRepository playerRepository;

    public WhoScoredScrapingService(NewLeagueRepository leagueRepository,
                                    TeamRepository teamRepository,
                                    WhoScoredRepository playerRepository) {
        this.leagueRepository = leagueRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public void scrape(String numberOfPlayers) {
        validateNumberOfPlayers(numberOfPlayers);
        String URL = "https://www.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=" +
            "0&isCurrent=true&playerId=&teamIds=&matchId=&stageId=&tournamentOptions=2,3,4,5,22&sortBy=Rating&sortAscending=&age=" +
            "&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=" +
            "&timeOfTheGameStart=&isMinApp=true&page=&includeZeroValues=&numberOfPlayersToPick=" + numberOfPlayers + "&incPens=";

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

            Map<Long, NewLeagueEntity> persistedLeagues = upsertLeagues(leagueEntities);
            Map<Long, TeamEntity> persistedTeams = upsertTeams(teamEntities);
            upsertPlayers(playerEntities, persistedTeams, persistedLeagues);
        }

    }

    private Map<Long, NewLeagueEntity> upsertLeagues(Map<Long, NewLeagueEntity> scrapedLeagues) {
        Map<Long, NewLeagueEntity> persisted = scrapedLeagues.isEmpty()
            ? new HashMap<>()
            : leagueRepository.findByExternalIdIn(scrapedLeagues.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(NewLeagueEntity::getExternalId, league -> league));

        List<NewLeagueEntity> newLeagues = new ArrayList<>();
        scrapedLeagues.forEach((externalId, scraped) -> {
            NewLeagueEntity existing = persisted.get(externalId);
            if (existing == null) {
                newLeagues.add(scraped);
                persisted.put(externalId, scraped);
            } else {
                existing.setName(scraped.getName());
                existing.setCountry(scraped.getCountry());
            }
        });
        leagueRepository.saveAll(newLeagues);
        return persisted;
    }

    private Map<Long, TeamEntity> upsertTeams(Map<Long, TeamEntity> scrapedTeams) {
        Map<Long, TeamEntity> persisted = scrapedTeams.isEmpty()
            ? new HashMap<>()
            : teamRepository.findByExternalIdIn(scrapedTeams.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(TeamEntity::getExternalId, team -> team));

        List<TeamEntity> newTeams = new ArrayList<>();
        scrapedTeams.forEach((externalId, scraped) -> {
            TeamEntity existing = persisted.get(externalId);
            if (existing == null) {
                newTeams.add(scraped);
                persisted.put(externalId, scraped);
            } else {
                existing.setName(scraped.getName());
            }
        });
        teamRepository.saveAll(newTeams);
        return persisted;
    }

    private void upsertPlayers(List<WhoScoredEntity> scrapedPlayers,
                               Map<Long, TeamEntity> teams,
                               Map<Long, NewLeagueEntity> leagues) {
        scrapedPlayers.forEach(player -> {
            player.setTeam(teams.get(player.getTeam().getExternalId()));
            player.setLeague(leagues.get(player.getLeague().getExternalId()));
        });

        Map<Long, WhoScoredEntity> persisted = scrapedPlayers.isEmpty()
            ? new HashMap<>()
            : playerRepository.findByExternalIdIn(scrapedPlayers.stream()
                    .map(WhoScoredEntity::getExternalId)
                    .collect(java.util.stream.Collectors.toSet())).stream()
                .collect(java.util.stream.Collectors.toMap(WhoScoredEntity::getExternalId, player -> player));

        List<WhoScoredEntity> newPlayers = new ArrayList<>();
        scrapedPlayers.forEach(scraped -> {
            WhoScoredEntity existing = persisted.get(scraped.getExternalId());
            if (existing == null) {
                newPlayers.add(scraped);
            } else {
                existing.updateFrom(scraped);
            }
        });
        playerRepository.saveAll(newPlayers);
    }

    private void validateNumberOfPlayers(String numberOfPlayers) {
        try {
            if (numberOfPlayers == null || Integer.parseInt(numberOfPlayers) <= 0) {
                throw new IllegalArgumentException("numberOfPlayers must be a positive integer");
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("numberOfPlayers must be a positive integer", exception);
        }
    }
}
