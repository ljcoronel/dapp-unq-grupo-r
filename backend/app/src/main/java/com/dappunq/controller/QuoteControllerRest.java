package com.dappunq.controller;

import com.dappunq.dto.valuation.RecalculationResult;
import com.dappunq.model.valuation.ExecutionType;
import com.dappunq.service.WeeklyValuationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/quotes")
public class QuoteControllerRest {

    private static final Logger log = LoggerFactory.getLogger(QuoteControllerRest.class);

    private final WeeklyValuationService weeklyValuationService;

    public QuoteControllerRest(WeeklyValuationService weeklyValuationService) {
        this.weeklyValuationService = weeklyValuationService;
    }

    @PostMapping("/recalculate")
    public ResponseEntity<?> recalculate() {
        log.info("Recibida peticion publica para recalculo manual de cotizaciones");
        try {
            RecalculationResult result = weeklyValuationService.execute(ExecutionType.MANUAL);
            log.info("Recalculo manual completado exitosamente: {}", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Fallo operativo en recalculo manual de cotizaciones: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "No se pudo completar el scraping o el recalculo; se conserva la ultima valuacion valida. " + e.getMessage()));
        }
    }
}
