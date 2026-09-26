package com.dappunq.unit;

import com.dappunq.dto.footballdata.PlayerPayload;
import com.dappunq.dto.footballdata.ScorerPayload;
import com.dappunq.dto.footballdata.TeamPayload;
import com.dappunq.service.PlayerMapper;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerMapperTest {
    @Test
    void filtersInvalidPlayersAndPreservesNullAndZeroStatistics() {
        var valid = new ScorerPayload(
                new PlayerPayload(10, "  Jugador  ", " "),
                new TeamPayload("Equipo"), 0, null, 2, 0);
        var invalid = new ScorerPayload(
                new PlayerPayload(-1, "Sin ID", null),
                new TeamPayload("Equipo"), 1, 1, 1, 1);

        var players = PlayerMapper.toPlayers(java.util.List.of(valid, invalid));

        assertThat(players).hasSize(1);
        assertThat(players.getFirst().name()).isEqualTo("Jugador");
        assertThat(players.getFirst().section()).isNull();
        assertThat(players.getFirst().statistics().playedMatches()).isZero();
        assertThat(players.getFirst().statistics().goals()).isNull();
        assertThat(players.getFirst().statistics().penalties()).isZero();
    }

    @Test
    void callerCanTruncateMappedPlayersToTen() {
        var scorers = IntStream.rangeClosed(1, 12)
                .mapToObj(id -> new ScorerPayload(
                        new PlayerPayload(id, "Jugador " + id, "Delantero"),
                        new TeamPayload("Equipo"), null, id, null, null))
                .toList();

        assertThat(PlayerMapper.toPlayers(scorers)).hasSize(12);
        assertThat(PlayerMapper.toPlayers(scorers).stream().limit(10)).hasSize(10);
    }
}
