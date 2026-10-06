package com.dappunq.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class WhoScoredEntity {
    @Id
    private Long id;
    private String name;
    private String team;
    private String age;
    private String league;
    private String appearances;
    private String minutesPlayed;
    private String goals;
    private String assists;
    private String yellowCards;
    private String redCards;
    private String avgShotsPerGame;
    private String passSuccessPercentage;
    private String aerialsWon;
    private String timesManOfTheMatch;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() { return id;}

    public void setName(String name) {
        this.name = name;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setAge(String age) {
        this.age = age;
    }
    public void setLeague(String league) {
        this.league = league;
    }

    public void setAppearances(String appearances) {
        this.appearances = appearances;
    }

    public String getAppearances() {
        return appearances;
    }

    public void setMinutesPlayed(String minutesPlayed) {
        this.minutesPlayed = minutesPlayed;
    }

    public String getMinutesPlayed() {
        return minutesPlayed;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    public String getGoals() {
        return goals;
    }

    public void setAssists(String assists) {
        this.assists = assists;
    }

    public String getAssists() {
        return assists;
    }

    public void setYellowCards(String yellowCards) {
        this.yellowCards = yellowCards;
    }

    public String getYellowCards() {
        return yellowCards;
    }

    public void setRedCards(String redCards) {
        this.redCards = redCards;
    }

    public String getRedCards() {
        return redCards;
    }

    public void setAvgShotsPerGame(String avgShotsPerGame) {
        this.avgShotsPerGame = avgShotsPerGame;
    }

    public String getAvgShotsPerGame() {
        return avgShotsPerGame;
    }

    public void setPassSuccessPercentage(String passSuccessPercentage) {
        this.passSuccessPercentage = passSuccessPercentage;
    }

    public String getPassSuccessPercentage() {
        return passSuccessPercentage;
    }

    public void setAerialsWon(String aerialsWon) {
        this.aerialsWon = aerialsWon;
    }

    public void setTimesManOfTheMatch(String timesManOfTheMatch) {
        this.timesManOfTheMatch = timesManOfTheMatch;
    }

    public String getTimesManOfTheMatch() {
        return timesManOfTheMatch;
    }

}
