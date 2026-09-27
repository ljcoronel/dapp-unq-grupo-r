package com.dappunq.unit;

import com.dappunq.dto.PlayerResponse;
import com.dappunq.dto.footballdata.PlayerPayload;
import com.dappunq.dto.footballdata.ScorerPayload;
import com.dappunq.dto.footballdata.TeamPayload;
import com.dappunq.service.PlayerMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerIncompleteDataTest {
    @Test
    void mapsMissingStatisticsToNullAndPreservesZero() {
        var player = PlayerMapper.toPlayer(new ScorerPayload(
                new PlayerPayload(42, "Jugador", "Delantero"),
                new TeamPayload("Equipo"), null, 0, null, 0));

        var response = PlayerResponse.from(player);

        assertThat(response.partidosJugados()).isNull();
        assertThat(response.goles()).isZero();
        assertThat(response.asistencias()).isNull();
        assertThat(response.penaltis()).isZero();
    }

    @Test
    void normalizesBlankSectionAndRejectsMissingIdentityFields() {
        var player = PlayerMapper.toPlayer(new ScorerPayload(
                new PlayerPayload(42, "  Jugador  ", "   "),
                new TeamPayload("  Equipo  "), null, null, null, null));

        assertThat(player.name()).isEqualTo("Jugador");
        assertThat(player.section()).isNull();
        assertThat(player.team()).isEqualTo("Equipo");

        assertThat(PlayerMapper.toPlayers(java.util.List.of(
                new ScorerPayload(new PlayerPayload(43, " ", "Defensa"),
                        new TeamPayload("Equipo"), null, null, null, null),
                new ScorerPayload(new PlayerPayload(44, "Jugador", "Defensa"),
                        new TeamPayload(" "), null, null, null, null))))
                .isEmpty();
    }

    @Test
    void rejectsInvalidPlayerIdentityWhenMappingOneRecord() {
        assertThatThrownBy(() -> PlayerMapper.toPlayer(new ScorerPayload(
                new PlayerPayload(0, "Jugador", "Defensa"),
                new TeamPayload("Equipo"), null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
