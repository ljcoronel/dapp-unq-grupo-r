package com.dappunq.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "whoscored_players")
public class WhoScoredEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private NewLeagueEntity league;

    @Column(name = "age")
    private Integer age;

    @Column(name = "height_cm")
    private Integer heightCM;

    @Column(name = "weight_kg")
    private Integer weightKG;

    @Column(name = "position")
    private String position;

    @Column(name = "appearances")
    private Integer appearances;

    @Column(name = "minutes_played")
    private Integer minutesPlayed;

    @Column(name = "goals")
    private Integer goals;

    @Column(name = "assists")
    private Integer assists;

    @Column(name = "yellow_cards")
    private Integer yellowCards;

    @Column(name = "red_cards")
    private Integer redCards;

    @Column(name = "avg_shots_per_game")
    private Float avgShotsPerGame;

    @Column(name = "pass_success_percentage")
    private Float passSuccessPercentage;

    @Column(name = "aerials_won")
    private Float aerialsWon;

    @Column(name = "times_man_of_the_match")
    private Integer timesManOfTheMatch;

    protected WhoScoredEntity() {
    }

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

    public Long getId() {
        return id;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TeamEntity getTeam() {
        return team;
    }

    public void setTeam(TeamEntity team) {
        this.team = team;
    }

    public NewLeagueEntity getLeague() {
        return league;
    }

    public void setLeague(NewLeagueEntity league) {
        this.league = league;
    }

    public void updateFrom(WhoScoredEntity source) {
        this.name = source.name;
        this.team = source.team;
        this.league = source.league;
        this.age = source.age;
        this.heightCM = source.heightCM;
        this.weightKG = source.weightKG;
        this.position = source.position;
        this.appearances = source.appearances;
        this.minutesPlayed = source.minutesPlayed;
        this.goals = source.goals;
        this.assists = source.assists;
        this.yellowCards = source.yellowCards;
        this.redCards = source.redCards;
        this.avgShotsPerGame = source.avgShotsPerGame;
        this.passSuccessPercentage = source.passSuccessPercentage;
        this.aerialsWon = source.aerialsWon;
        this.timesManOfTheMatch = source.timesManOfTheMatch;
    }

    public Integer getAge() {
        return age;
    }

    public Integer getHeightCM() {
        return heightCM;
    }

    public Integer getWeightKG() {
        return weightKG;
    }

    public String getPosition() {
        return position;
    }

    public Integer getAppearances() {
        return appearances;
    }

    public Integer getMinutesPlayed() {
        return minutesPlayed;
    }

    public Integer getGoals() {
        return goals;
    }

    public Integer getAssists() {
        return assists;
    }

    public Integer getYellowCards() {
        return yellowCards;
    }

    public Integer getRedCards() {
        return redCards;
    }

    public Float getAvgShotsPerGame() {
        return avgShotsPerGame;
    }

    public Float getPassSuccessPercentage() {
        return passSuccessPercentage;
    }

    public Float getAerialsWon() {
        return aerialsWon;
    }

    public Integer getTimesManOfTheMatch() {
        return timesManOfTheMatch;
    }
}
