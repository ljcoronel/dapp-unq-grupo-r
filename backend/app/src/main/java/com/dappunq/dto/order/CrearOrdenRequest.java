package com.dappunq.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CrearOrdenRequest(
        @NotNull(message = "El identificador del usuario es obligatorio")
        @Positive(message = "El identificador del usuario debe ser positivo")
        Long usuarioId,
        @NotNull(message = "El identificador del jugador es obligatorio")
        @Positive(message = "El identificador del jugador debe ser positivo")
        Long playerId,
        @NotNull(message = "La cantidad de tokens es obligatoria")
        @Min(value = 1, message = "La cantidad de tokens debe ser un entero positivo")
        Integer quantity,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        @Digits(integer = 13, fraction = 2, message = "El precio debe tener como máximo dos decimales")
        BigDecimal price,
        @NotNull(message = "La tolerancia es obligatoria")
        @DecimalMin(value = "0.00", message = "La tolerancia no puede ser negativa")
        @Digits(integer = 13, fraction = 2, message = "La tolerancia debe tener como máximo dos decimales")
        BigDecimal tolerance
) {
}
