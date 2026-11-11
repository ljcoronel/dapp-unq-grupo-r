package com.dappunq.dto.valuation;

import java.math.BigDecimal;

public record HoldingResponse(Long id, Long usuarioId, Long jugadorId, Integer tokens,
                              BigDecimal valorTotal, BigDecimal valorUnitario) {}
