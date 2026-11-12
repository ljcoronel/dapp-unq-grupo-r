CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_valuation
    ON valuaciones (usuario_id, jugador_id)
    WHERE is_current = true;
