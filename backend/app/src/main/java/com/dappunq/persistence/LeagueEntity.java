package com.dappunq.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leagues")
public class LeagueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String country;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "player_order")
    private List<PlayerEntity> players = new ArrayList<>();

    protected LeagueEntity() {
    }

    public LeagueEntity(String code, String name, String country) {
        this.code = code;
        this.name = name;
        this.country = country;
    }

    public void replacePlayers(List<PlayerEntity> replacement) {
        players.clear();
        replacement.forEach(this::addPlayer);
    }

    public void addPlayer(PlayerEntity player) {
        player.setLeague(this);
        players.add(player);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public List<PlayerEntity> getPlayers() { return players; }
}
