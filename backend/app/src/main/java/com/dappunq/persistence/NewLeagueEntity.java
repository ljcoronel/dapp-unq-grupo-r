package com.dappunq.persistence;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

public class NewLeagueEntity {

    private Long id;

    private String name;

    private String country;

    private List<WhoScoredEntity> players = new ArrayList<>();

    protected NewLeagueEntity() {
    }

    public NewLeagueEntity(String name, String country) {
        this.name = name;
        this.country = country;
    }
}
