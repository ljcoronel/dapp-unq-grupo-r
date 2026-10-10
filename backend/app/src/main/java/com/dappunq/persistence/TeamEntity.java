package com.dappunq.persistence;

public class TeamEntity {

    private Long id;
    private Long external_id;
    private String name;

    public TeamEntity(Long external_id, String name) {
        this.external_id = external_id;
        this.name = name;
    }
}
