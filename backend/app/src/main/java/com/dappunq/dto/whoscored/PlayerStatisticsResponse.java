package com.dappunq.dto.whoscored;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerStatisticsResponse(
    List<PlayerStatDTO> playerTableStats
) {
}
