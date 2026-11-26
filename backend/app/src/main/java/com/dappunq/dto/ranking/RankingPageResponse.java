package com.dappunq.dto.ranking;

import java.util.List;

public record RankingPageResponse(List<RankingPlayerResponse> items, long total, int page, int limit) {
}
