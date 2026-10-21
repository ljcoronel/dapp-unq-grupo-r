package com.dappunq.dto.valuation; import com.dappunq.model.valuation.*; import jakarta.validation.constraints.*; import java.math.*; import java.util.*;
public record StrategyRequest(@NotNull StrategyType estrategia,@NotEmpty Map<String,@NotNull @DecimalMin("0") BigDecimal> pesos) {}
