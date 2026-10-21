package com.dappunq.dto.valuation; import com.dappunq.model.valuation.*; import java.time.*; import java.math.*; import java.util.*;
public record StrategyStateResponse(StrategyType estrategia,Map<String,BigDecimal> pesos,StrategyStatus estado,Instant vigenteDesde) {}
