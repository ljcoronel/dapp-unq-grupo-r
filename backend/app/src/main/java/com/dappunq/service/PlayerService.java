package com.dappunq.service;

import com.dappunq.dto.PlayerResponse;

import java.util.List;

public interface PlayerService {

    List<PlayerResponse> findCatalog();

    PlayerResponse findPlayer(Integer id);
}
