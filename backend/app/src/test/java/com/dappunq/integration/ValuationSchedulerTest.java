package com.dappunq.integration;

import com.dappunq.config.ValuationScheduler;
import com.dappunq.dto.valuation.RecalculationResult;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.service.WeeklyValuationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValuationSchedulerTest {

    @Mock
    private WeeklyValuationService weeklyValuationService;

    @InjectMocks
    private ValuationScheduler valuationScheduler;

    @Test
    void delegaAlServicioDeValuacionSemanalConTipoDominical() {
        when(weeklyValuationService.execute(eq(ExecutionType.DOMINICAL)))
                .thenReturn(new RecalculationResult("COMPLETED", 50, 10, Instant.now()));

        valuationScheduler.executeWeeklyValuation();

        verify(weeklyValuationService).execute(eq(ExecutionType.DOMINICAL));
    }

    @Test
    void anotacionScheduledTieneCronYZonaHorariaConfigurada() throws NoSuchMethodException {
        Method method = ValuationScheduler.class.getMethod("executeWeeklyValuation");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertNotNull(scheduled, "El metodo executeWeeklyValuation debe estar anotado con @Scheduled");
        assertEquals("${valuation.weekly.cron:0 0 3 ? * SUN}", scheduled.cron());
        assertEquals("${spring.task.scheduling.time-zone:America/Argentina/Buenos_Aires}", scheduled.zone());
    }
}
