package com.dappunq.dto.order;

import com.dappunq.model.order.Orden;

import java.math.BigDecimal;
import java.time.Instant;

public record OrdenPendienteResponse(
        Long orderId,
        String operation,
        Long playerId,
        Integer quantity,
        BigDecimal price,
        BigDecimal tolerance,
        String status,
        Instant issuedAt
) {
    public static OrdenPendienteResponse from(Orden orden) {
        return new OrdenPendienteResponse(orden.getId(), orden.getTipoOperacion().name(),
                orden.getJugadorId(), orden.getCantidad(), orden.getPrecio(), orden.getTolerancia(),
                orden.getEstado().name(), orden.getFechaEmision());
    }
}
