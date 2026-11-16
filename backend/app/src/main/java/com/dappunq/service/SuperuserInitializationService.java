package com.dappunq.service;

import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.model.valuation.Metric;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.ValuationPersistenceMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class SuperuserInitializationService {

    public static final String DEFAULT_SUPERUSER_NAME = "superuser";

    private final JpaUserEntityRepository userRepository;
    private final StrategyConfigRepository strategyConfigRepository;
    private final HoldingsValuationService holdingsValuationService;
    private final ValuationPersistenceMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final InitialHoldingsService initialHoldingsService;

    @Value("${valuation.superuser.username:superuser}")
    private String superuserUsername = DEFAULT_SUPERUSER_NAME;

    public SuperuserInitializationService(JpaUserEntityRepository userRepository,
                                          StrategyConfigRepository strategyConfigRepository,
                                          ValuationPersistenceMapper mapper,
                                          PasswordEncoder passwordEncoder,
                                          HoldingsValuationService holdingsValuationService,
                                          InitialHoldingsService initialHoldingsService) {
        this.userRepository = userRepository;
        this.strategyConfigRepository = strategyConfigRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.holdingsValuationService = holdingsValuationService;
        this.initialHoldingsService = initialHoldingsService;
    }

    @Transactional
    public UserEntity getOrCreateSuperuser() {
        return userRepository.findByNombreIgnoreCase(superuserUsername)
                .orElseGet(() -> userRepository.save(new UserEntity(
                        superuserUsername,
                        passwordEncoder.encode("superuser_password")
                )));
    }

    @Transactional
    public StrategyConfigEntity initializeSuperuserStrategy(UserEntity superuser) {
        return strategyConfigRepository.findFirstByUserIdAndEstado(superuser.getId(), StrategyStatus.ACTUAL)
                .orElseGet(() -> {
                    Map<Metric, BigDecimal> weights = new HashMap<>();
                    weights.put(Metric.goals, BigDecimal.valueOf(30));
                    weights.put(Metric.assists, BigDecimal.valueOf(25));
                    weights.put(Metric.passSuccessPercentage, BigDecimal.valueOf(15));
                    weights.put(Metric.aerialsWon, BigDecimal.valueOf(15));
                    weights.put(Metric.timesManOfTheMatch, BigDecimal.valueOf(15));

                    StrategyConfigEntity config = new StrategyConfigEntity(
                            superuser,
                            StrategyType.ESTRATEGIA_A,
                            mapper.write(weights),
                            StrategyStatus.ACTUAL,
                            Instant.now()
                    );
                    return strategyConfigRepository.save(config);
                });
    }

    @Transactional
    public void initializeSuperuser() {
        UserEntity superuser = getOrCreateSuperuser();
        initialHoldingsService.initializeHoldingsForAllPlayers(superuser);
        if (strategyConfigRepository.findFirstByUserIdAndEstado(superuser.getId(), StrategyStatus.ACTUAL).isEmpty()) {
            StrategyConfigEntity config = initializeSuperuserStrategy(superuser);
            holdingsValuationService.revalue(superuser.getId(), config, ExecutionType.MANUAL);
        }
    }
}
