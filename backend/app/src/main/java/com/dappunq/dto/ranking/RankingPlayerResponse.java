package com.dappunq.dto.ranking;

import java.math.BigDecimal;

public record RankingPlayerResponse(int position, long playerId, String name, String team, String league,
                                    String positionInField, BigDecimal representativeValue, String origin) {
}
