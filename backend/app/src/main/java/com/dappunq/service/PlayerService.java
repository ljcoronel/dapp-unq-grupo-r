package com.dappunq.service;

import com.dappunq.dto.PlayerResponse;

import java.util.List;

public interface PlayerService {

    List<PlayerResponse> getAll();

    PlayerResponse findPlayer(Integer id);
}
