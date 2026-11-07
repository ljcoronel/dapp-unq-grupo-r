package com.dappunq.dto.order;

import com.dappunq.model.order.Orden;

import java.math.BigDecimal;
import java.time.Instant;

public record TransaccionEjecutadaResponse(
        Long orderId,
        String operation,
        Long playerId,
        Integer quantity,
        BigDecimal settledPrice,
        Long buyerId,
        Long sellerId,
        Instant issuedAt,
        Instant executedAt
) {
    public static TransaccionEjecutadaResponse from(Orden orden) {
        return new TransaccionEjecutadaResponse(orden.getId(), orden.getTipoOperacion().name(),
                orden.getJugadorId(), orden.getCantidad(), orden.getPrecioLiquidado(),
                orden.getCompradorId(), orden.getVendedorId(), orden.getFechaEmision(),
                orden.getFechaEjecucion());
    }
}
