package com.dappunq.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "players", indexes = @Index(name = "idx_players_league", columnList = "league_id"))
public class PlayerEntity {
    @Id
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private LeagueEntity league;

    @Column(nullable = false)
    private String name;

    private String section;

    @Column(nullable = false)
    private String team;

    private Integer playedMatches;
    private Integer goals;
    private Integer assists;
    private Integer penalties;

    protected PlayerEntity() {
    }

    public PlayerEntity(Integer id, String name, String section, String team,
                        Integer playedMatches, Integer goals, Integer assists, Integer penalties) {
        this.id = id;
        this.name = name;
        this.section = section;
        this.team = team;
        this.playedMatches = playedMatches;
        this.goals = goals;
        this.assists = assists;
        this.penalties = penalties;
    }

    void setLeague(LeagueEntity league) { this.league = league; }
    public Integer getId() { return id; }
    public LeagueEntity getLeague() { return league; }
    public String getName() { return name; }
    public String getSection() { return section; }
    public String getTeam() { return team; }
    public Integer getPlayedMatches() { return playedMatches; }
    public Integer getGoals() { return goals; }
    public Integer getAssists() { return assists; }
    public Integer getPenalties() { return penalties; }
}
