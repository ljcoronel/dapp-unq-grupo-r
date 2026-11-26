CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_valuation
    ON valuaciones (usuario_id, jugador_id)
    WHERE is_current = true;

CREATE INDEX IF NOT EXISTS idx_players_league_team_position
    ON players (league_id, team_id, position);

CREATE INDEX IF NOT EXISTS idx_players_name_external_id
    ON players (name, external_id);

CREATE INDEX IF NOT EXISTS idx_valuaciones_current_player_user_strategy
    ON valuaciones (is_current, jugador_id, usuario_id, estrategia);

CREATE INDEX IF NOT EXISTS idx_estrategia_status_user_type
    ON configuraciones_estrategia (estado, usuario_id, tipoestrategia);

CREATE INDEX IF NOT EXISTS idx_tenencias_player_user_tokens
    ON tenencias (jugador_id, usuario_id, tokens);

CREATE TABLE IF NOT EXISTS metric_normalization (
    metric VARCHAR(64) PRIMARY KEY,
    minimum_value NUMERIC(19, 8) NOT NULL,
    maximum_value NUMERIC(19, 8) NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
