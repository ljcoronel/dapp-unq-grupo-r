package com.dappunq.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class League {
    public static final int MAX_PLAYERS = 10;

    private final String code;
    private final String name;
    private final String country;
    private final List<Player> players;

    public League(String code, String name, String country, List<Player> players) {
        this.code = requireText(code, "El código de liga es obligatorio");
        this.name = requireText(name, "El nombre de liga es obligatorio");
        this.country = requireText(country, "El país de liga es obligatorio");
        if (players == null || players.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException("Una liga no puede superar diez jugadores");
        }
        this.players = List.copyOf(players);
    }

    public League(String code, String name, String country) {
        this(code, name, country, List.of());
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public String code() { return code; }
    public String name() { return name; }
    public String country() { return country; }
    public List<Player> players() { return Collections.unmodifiableList(new ArrayList<>(players)); }
}
