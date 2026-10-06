package com.dappunq.persistence;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "new_leagues")
public class NewLeagueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String country;

    @OneToMany(mappedBy = "new_league", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "WhoScored_order")
    private List<WhoScoredEntity> players = new ArrayList<>();

    protected NewLeagueEntity() {
    }

    public NewLeagueEntity(String name, String country) {
        this.name = name;
        this.country = country;
    }
}
