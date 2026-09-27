package com.dappunq.config;

import java.util.List;

public final class LeagueCatalogProperties {
    private static final List<LeagueDefinition> LEAGUES = List.of(
            new LeagueDefinition("PL", "Premier League", "Inglaterra"),
            new LeagueDefinition("BL1", "Bundesliga", "Alemania"),
            new LeagueDefinition("PD", "Primera División", "España"),
            new LeagueDefinition("SA", "Serie A", "Italia"),
            new LeagueDefinition("FL1", "Ligue 1", "Francia")
    );

    private LeagueCatalogProperties() {
    }

    public static List<LeagueDefinition> leagues() {
        return LEAGUES;
    }

    public static LeagueDefinition findByCode(String code) {
        return LEAGUES.stream()
                .filter(league -> league.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Código de liga no configurado"));
    }

    public record LeagueDefinition(String code, String name, String country) {
        public LeagueDefinition {
            if (code == null || code.isBlank() || name == null || name.isBlank()
                    || country == null || country.isBlank()) {
                throw new IllegalArgumentException("La configuración de liga es obligatoria");
            }
        }
    }
}
