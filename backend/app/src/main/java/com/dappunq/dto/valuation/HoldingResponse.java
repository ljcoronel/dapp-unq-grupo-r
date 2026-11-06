package com.dappunq.dto.valuation; import java.math.*;
public record HoldingResponse(Long id,Long usuarioId,Long jugadorId,Integer tokens,BigDecimal valorTotal,BigDecimal valorUnitario) {}
