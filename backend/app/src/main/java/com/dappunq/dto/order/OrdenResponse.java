package com.dappunq.dto.order;

import com.dappunq.model.order.Orden;

import java.math.BigDecimal;
import java.time.Instant;

public record OrdenResponse(
        Long orderId,
        Long userId,
        Long playerId,
        String operation,
        Integer quantity,
        BigDecimal price,
        BigDecimal tolerance,
        String status,
        Instant issuedAt,
        Instant executedAt,
        Instant cancelledAt,
        BigDecimal settledPrice,
        Long buyerId,
        Long sellerId
) {
    public static OrdenResponse from(Orden orden) {
        return new OrdenResponse(orden.getId(), orden.getUsuarioId(), orden.getJugadorId(),
                orden.getTipoOperacion().name(), orden.getCantidad(), orden.getPrecio(),
                orden.getTolerancia(), orden.getEstado().name(), orden.getFechaEmision(),
                orden.getFechaEjecucion(), orden.getFechaCancelacion(), orden.getPrecioLiquidado(),
                orden.getCompradorId(), orden.getVendedorId());
    }
}
