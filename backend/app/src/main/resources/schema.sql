CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_valuation
    ON valuaciones (usuario_id, jugador_id)
    WHERE is_current = true;

CREATE TABLE IF NOT EXISTS metric_normalization (
    metric VARCHAR(64) PRIMARY KEY,
    minimum_value NUMERIC(19, 8) NOT NULL,
    maximum_value NUMERIC(19, 8) NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
