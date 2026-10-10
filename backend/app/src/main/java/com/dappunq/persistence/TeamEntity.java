package com.dappunq.persistence;

public class TeamEntity {

    private Long id;
    private Long externalId;
    private String name;

    public TeamEntity(Long externalId, String name) {
        this.externalId = externalId;
        this.name = name;
    }
}
