package com.dappunq.service;

import com.dappunq.model.valuation.StrategyConfig;
import com.dappunq.model.valuation.StrategyState;

public interface StrategyConfigurationService {

    StrategyState configure(Long id, StrategyConfig r);
    StrategyState get(Long id);
}
