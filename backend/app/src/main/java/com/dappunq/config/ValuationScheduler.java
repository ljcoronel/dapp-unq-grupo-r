package com.dappunq.config;

import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.service.WeeklyValuationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ValuationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ValuationScheduler.class);

    private final WeeklyValuationService weeklyValuationService;

    public ValuationScheduler(WeeklyValuationService weeklyValuationService) {
        this.weeklyValuationService = weeklyValuationService;
    }

    @Scheduled(cron = "${valuation.weekly.cron:0 0 3 ? * SUN}", zone = "${spring.task.scheduling.time-zone:America/Argentina/Buenos_Aires}")
    public void executeWeeklyValuation() {
        log.info("Iniciando valuacion semanal automatica...");
        try {
            weeklyValuationService.execute(ExecutionType.DOMINICAL);
            log.info("Valuacion semanal completada exitosamente.");
        } catch (Exception e) {
            log.error("Error durante la valuacion semanal automatica: {}", e.getMessage(), e);
        }
    }
}
