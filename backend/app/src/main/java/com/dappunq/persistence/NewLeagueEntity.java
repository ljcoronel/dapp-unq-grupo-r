package com.dappunq.persistence;

public class NewLeagueEntity {

    private Long id;

    private Long externalId;

    private String name;

    private String country;

    protected NewLeagueEntity() {
    }

    public NewLeagueEntity(Long externalId, String name, String country) {
        this.externalId = externalId;
        this.name = name;
        this.country = country;
    }
}
