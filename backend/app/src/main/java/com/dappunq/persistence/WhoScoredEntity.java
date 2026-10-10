package com.dappunq.persistence;


public class WhoScoredEntity {

    private Long id;
    private Long externalId;
    private String name;
    private TeamEntity team;
    private NewLeagueEntity league;
    private Integer age;
    private Integer heightCM;
    private Integer weightKG;
    private String position;
    private Integer appearances;
    private Integer minutesPlayed;
    private Integer goals;
    private Integer assists;
    private Integer yellowCards;
    private Integer redCards;
    private Float avgShotsPerGame;
    private Float passSuccessPercentage;
    private Float aerialsWon;
    private Integer timesManOfTheMatch;

    public WhoScoredEntity(Long externalId, String name, TeamEntity teamEntity,
                           NewLeagueEntity newLeagueEntity, Integer age,
                           Integer height, Integer weight, String position, Integer apps,
                           Integer minsPlayed, Integer goal, Integer assists,
                           Integer yellowCard, Integer redCard, Float shotsPerGame,
                           Float passSuccess, Float aerialsWonPerGame, Integer manOfTheMatch) {
        this.externalId = externalId;
        this.name = name;
        this.team = teamEntity;
        this.league = newLeagueEntity;
        this.age = age;
        this.heightCM = height;
        this.weightKG = weight;
        this.position = position;
        this.appearances = apps;
        this.minutesPlayed = minsPlayed;
        this.goals = goal;
        this.assists = assists;
        this.yellowCards = yellowCard;
        this.redCards = redCard;
        this.avgShotsPerGame = shotsPerGame;
        this.passSuccessPercentage = passSuccess;
        this.aerialsWon = aerialsWonPerGame;
        this.timesManOfTheMatch = manOfTheMatch;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() { return id;}

    public void setName(String name) {
        this.name = name;
    }

    public void setTeam(TeamEntity team) {
        this.team = team;
    }

    public void setLeague(NewLeagueEntity league) {
        this.league = league;
    }
}
