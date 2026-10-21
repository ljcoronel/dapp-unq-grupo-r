package com.dappunq.dto.valuation; import java.time.*;
public record RecalculationResult(String status,int playersScraped,int recalculatedHoldings,Instant executedAt) {}
