package com.dappunq.service;

import com.dappunq.persistence.NewLeagueEntity;
import com.dappunq.persistence.WhoScoredEntity;
import com.microsoft.playwright.*;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

@Service
public class WhoScoredScrapingService {

    public WhoScoredScrapingService() {
    }

    public void scrape() {
        String baseURL = "https://www.whoscored.com/regions";
        ArrayList<String> urls = getUrls();

        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(true);
            Browser browser = playwright.chromium().launch(options);

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"));

            Page page = context.newPage();

            urls.forEach(url -> {
                page.navigate(baseURL + url);

                page.waitForSelector("table#top-player-stats-summary-grid");

                Locator playerRows = page.locator("table#top-player-stats-summary-grid tbody tr");
                String league = page.locator("select#tournaments option[selected]").textContent();
                String country = page.locator("div#breadcrumb-nav span.iconize").textContent();
                int count = playerRows.count();
                for (int i = 0; i < count; i++) {
                    Locator row = playerRows.nth(i);

                    String name = row.locator("span.iconize").textContent();
                    String team = row.locator("td.col12-lg-2.col12-m-3.col12-s-4.col12-xs-5.grid-abs.overflow-text").locator("span.team-name").textContent();
                    String age = row.locator("span[style*='padding-left: 3px']").textContent();
                    String appearances = row.locator("td").nth(2).textContent();
                    String minutesPlayed = row.locator("td").nth(3).textContent();
                    String goals = row.locator("td").nth(4).textContent();
                    String assists = row.locator("td").nth(5).textContent();
                    String yellowCards = row.locator("td").nth(6).textContent();
                    String redCards = row.locator("td").nth(7).textContent();
                    String avgShotsPerGame = row.locator("td").nth(8).textContent();
                    String passSuccessPercentage = row.locator("td").nth(9).textContent();
                    String aerialsWon = row.locator("td").nth(10).textContent();
                    String timesManOfTheMatch = row.locator("td").nth(11).textContent();

                    WhoScoredEntity player = new WhoScoredEntity();
                    player.setName(name != null ? name.trim() : "Unknown");
                    player.setTeam(team != null ? team.trim().substring(0, team.length() - 1) : "Unknown");
                    player.setAge(age != null ? age.trim() : "Unknown");
                    player.setLeague(new NewLeagueEntity(league != null ? league.trim() : "Unknown", country));
                    player.setAppearances(appearances != null ? appearances.trim() : "Unknown");
                    player.setMinutesPlayed(minutesPlayed != null ? minutesPlayed.trim() : "Unknown");
                    player.setGoals(goals != null ? goals.trim() : "Unknown");
                    player.setAssists(assists != null ? assists.trim() : "Unknown");
                    player.setYellowCards(yellowCards != null ? yellowCards.trim() : "Unknown");
                    player.setRedCards(redCards != null ? redCards.trim() : "Unknown");
                    player.setAvgShotsPerGame(avgShotsPerGame != null ? avgShotsPerGame.trim() : "Unknown");
                    player.setPassSuccessPercentage(passSuccessPercentage != null ? passSuccessPercentage.trim() : "Unknown");
                    player.setAerialsWon(aerialsWon != null ? aerialsWon.trim() : "Unknown");
                    player.setTimesManOfTheMatch(timesManOfTheMatch != null ? timesManOfTheMatch.trim() : "Unknown");

                    // Save the player data to the database
                    // TODO
                }
            });
            browser.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to scrape WhoScored data: " + e.getMessage(), e);
        }
    }

    private static @NonNull ArrayList<String> getUrls() {
        String laLiga = "/206/tournaments/4/seasons/11213/stages/25662/playerstatistics/spain-laliga-2026-2027";
        String bundesliga = "/81/tournaments/3/seasons/11217/stages/25666/playerstatistics/germany-bundesliga-2026-2027";
        String serieA = "/108/tournaments/5/seasons/11126/stages/25518/playerstatistics/italy-serie-a-2026-2027";
        String premierLeague = "/252/tournaments/2/seasons/11141/stages/25544/playerstatistics/england-premier-league-2026-2027";
        String ligue1 = "/74/tournaments/22/seasons/11150/stages/25554/playerstatistics/france-ligue-1-2026-2027";
        return new ArrayList<>(Arrays.asList(laLiga, bundesliga, serieA, premierLeague, ligue1));
    }
}
