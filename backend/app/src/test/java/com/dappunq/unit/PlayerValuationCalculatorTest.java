package com.dappunq.unit;

import com.dappunq.model.valuation.Metric;
import com.dappunq.model.valuation.PlayerSnapshot;
import com.dappunq.model.valuation.PlayerValuationCalculator;
import com.dappunq.model.valuation.StrategyConfig;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.model.valuation.ValuationDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerValuationCalculatorTest {

    private PlayerValuationCalculator calculator;
    private Map<Metric, BigDecimal> weightsA;
    private Map<Metric, BigDecimal> weightsB;

    @BeforeEach
    void setUp() {
        calculator = new PlayerValuationCalculator();

        weightsA = new HashMap<>();
        weightsA.put(Metric.goals, BigDecimal.valueOf(30));
        weightsA.put(Metric.assists, BigDecimal.valueOf(25));
        weightsA.put(Metric.passSuccessPercentage, BigDecimal.valueOf(15));
        weightsA.put(Metric.aerialsWon, BigDecimal.valueOf(15));
        weightsA.put(Metric.timesManOfTheMatch, BigDecimal.valueOf(15));

        weightsB = new HashMap<>();
        weightsB.put(Metric.passSuccessPercentage, BigDecimal.valueOf(35));
        weightsB.put(Metric.aerialsWon, BigDecimal.valueOf(20));
        weightsB.put(Metric.minutesPlayed, BigDecimal.valueOf(20));
        weightsB.put(Metric.appearances, BigDecimal.valueOf(20));
    }

    @Test
    void metricasEnumCerradas() {
        assertEquals(8, Metric.values().length);
        assertEquals(StrategyType.ESTRATEGIA_B, StrategyType.valueOf("ESTRATEGIA_B"));
    }

    @Test
    @DisplayName("Normalizacion [0,1]: el jugador con minimos obtiene 0.00 y el de maximos 1.00")
    void normalizacionExtremos() {
        StrategyConfig configA = new StrategyConfig(StrategyType.ESTRATEGIA_A, weightsA);

        PlayerSnapshot minPlayer = new PlayerSnapshot(1L, "Forward", 0, 0, 50.0f, 0.0f, 0, 0, 0);
        PlayerSnapshot maxPlayer = new PlayerSnapshot(2L, "Forward", 20, 10, 100.0f, 10.0f, 5, 1000, 20);
        List<PlayerSnapshot> snapshot = List.of(minPlayer, maxPlayer);

        BigDecimal minValuation = calculator.calculate(minPlayer, configA, snapshot, BigDecimal.ZERO);
        BigDecimal maxValuation = calculator.calculate(maxPlayer, configA, snapshot, BigDecimal.ZERO);

        assertEquals(new BigDecimal("0.00"), minValuation);
        assertEquals(new BigDecimal("2.00"), maxValuation);
    }

    @Test
    @DisplayName("Normalizacion [0,1]: jugador intermedio calcula valor proporcional")
    void normalizacionIntermedia() {
        StrategyConfig configA = new StrategyConfig(StrategyType.ESTRATEGIA_A, weightsA);

        PlayerSnapshot p1 = new PlayerSnapshot(1L, "Midfielder", 0, 0, 0.0f, 0.0f, 0, 0, 0);
        PlayerSnapshot p2 = new PlayerSnapshot(2L, "Midfielder", 10, 10, 100.0f, 10.0f, 10, 1000, 20);
        // pMid has exactly half of all metrics -> each normalized metric is 0.50
        PlayerSnapshot pMid = new PlayerSnapshot(3L, "Midfielder", 5, 5, 50.0f, 5.0f, 5, 500, 10);
        List<PlayerSnapshot> snapshot = List.of(p1, p2, pMid);

        BigDecimal valuation = calculator.calculate(pMid, configA, snapshot, BigDecimal.ZERO);
        // 0.5 * 30% + 0.5 * 25% + 0.5 * 15% + 0.5 * 15% + 0.5 * 15% = 0.50
        assertEquals(new BigDecimal("1.00"), valuation);
    }

    @Test
    @DisplayName("Metricas faltantes (null) se normalizan a 0 y el jugador sigue siendo valuado")
    void metricasFaltantesNormalizanEnCero() {
        StrategyConfig configA = new StrategyConfig(StrategyType.ESTRATEGIA_A, weightsA);

        PlayerSnapshot p1 = new PlayerSnapshot(1L, "Forward", 0, 0, 50.0f, 0.0f, 0, 100, 5);
        PlayerSnapshot p2 = new PlayerSnapshot(2L, "Forward", 10, 10, 100.0f, 10.0f, 10, 200, 10);
        // pPartial has null assists and null timesManOfTheMatch
        PlayerSnapshot pPartial = new PlayerSnapshot(3L, "Forward", 10, null, 100.0f, 10.0f, null, 200, 10);
        List<PlayerSnapshot> snapshot = List.of(p1, p2, pPartial);

        BigDecimal valuation = calculator.calculate(pPartial, configA, snapshot, BigDecimal.ZERO);
        // goals=1.0 (30%) + assists=0.0 (0%) + pass=1.0 (15%) + aerials=1.0 (15%) + motm=0.0 (0%) = 0.60
        assertEquals(new BigDecimal("1.20"), valuation);
    }

    @Test
    @DisplayName("Cuando todos los valores disponibles son iguales (min == max), la metrica normalizada es 0 para evitar division por cero")
    void todosValoresIgualesNormalizanEnCero() {
        StrategyConfig configA = new StrategyConfig(StrategyType.ESTRATEGIA_A, weightsA);

        // All players have identical goals (5)
        PlayerSnapshot p1 = new PlayerSnapshot(1L, "Forward", 5, 0, 50.0f, 0.0f, 0, 0, 0);
        PlayerSnapshot p2 = new PlayerSnapshot(2L, "Forward", 5, 10, 100.0f, 10.0f, 10, 1000, 20);
        List<PlayerSnapshot> snapshot = List.of(p1, p2);

        BigDecimal valP2 = calculator.calculate(p2, configA, snapshot, BigDecimal.ZERO);
        // goals is 0 because min == max, assists=1.0 (25%) + pass=1.0 (15%) + aerials=1.0 (15%) + motm=1.0 (15%) = 0.70
        assertEquals(new BigDecimal("1.40"), valP2);
    }

    @Test
    @DisplayName("Regla Defender en Estrategia B: suma 5% (0.05) solo si position es Defender")
    void reglaDefenderEnEstrategiaB() {
        StrategyConfig configB = new StrategyConfig(StrategyType.ESTRATEGIA_B, weightsB);

        PlayerSnapshot defenderMin = new PlayerSnapshot(1L, "Defender", 0, 0, 50.0f, 0.0f, 0, 100, 5);
        PlayerSnapshot forwardMin = new PlayerSnapshot(2L, "Forward", 0, 0, 50.0f, 0.0f, 0, 100, 5);
        PlayerSnapshot defenderMax = new PlayerSnapshot(3L, "Defender", 0, 0, 100.0f, 10.0f, 0, 1000, 25);
        PlayerSnapshot forwardMax = new PlayerSnapshot(4L, "Forward", 0, 0, 100.0f, 10.0f, 0, 1000, 25);
        List<PlayerSnapshot> snapshot = List.of(defenderMin, forwardMin, defenderMax, forwardMax);

        // Min stats: 0 from metrics. Defender gets +0.05, Forward gets 0.00
        BigDecimal valDefMin = calculator.calculate(defenderMin, configB, snapshot, BigDecimal.ZERO);
        BigDecimal valFwdMin = calculator.calculate(forwardMin, configB, snapshot, BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.10"), valDefMin);
        assertEquals(new BigDecimal("0.00"), valFwdMin);

        // Max stats: 95% (0.95) from metrics. Defender gets 0.95 + 0.05 = 1.00, Forward gets 0.95
        BigDecimal valDefMax = calculator.calculate(defenderMax, configB, snapshot, BigDecimal.ZERO);
        BigDecimal valFwdMax = calculator.calculate(forwardMax, configB, snapshot, BigDecimal.ZERO);
        assertEquals(new BigDecimal("2.00"), valDefMax);
        assertEquals(new BigDecimal("1.90"), valFwdMax);
    }

    @Test
    @DisplayName("Regla Defender NO aplica en Estrategia A")
    void reglaDefenderNoAplicaEnEstrategiaA() {
        StrategyConfig configA = new StrategyConfig(StrategyType.ESTRATEGIA_A, weightsA);

        PlayerSnapshot defender = new PlayerSnapshot(1L, "Defender", 0, 0, 50.0f, 0.0f, 0, 0, 0);
        PlayerSnapshot other = new PlayerSnapshot(2L, "Forward", 10, 10, 100.0f, 10.0f, 10, 0, 0);
        List<PlayerSnapshot> snapshot = List.of(defender, other);

        BigDecimal valDef = calculator.calculate(defender, configA, snapshot, BigDecimal.ZERO);
        // Defender has min stats in A -> 0.00 (no 5% bonus)
        assertEquals(new BigDecimal("0.00"), valDef);
    }

    @Test
    @DisplayName("Redondeo monetario a dos decimales con HALF_UP")
    void redondeoMonetarioADosDecimales() {
        StrategyConfig configA = new StrategyConfig(StrategyType.ESTRATEGIA_A, weightsA);

        // Range min 0, max 10
        PlayerSnapshot pMin = new PlayerSnapshot(1L, "Forward", 0, 0, 0f, 0f, 0, 0, 0);
        PlayerSnapshot pMax = new PlayerSnapshot(2L, "Forward", 10, 10, 10f, 10f, 10, 0, 0);

        // p1: goals = 5 (0.5 * 30% = 0.15)
        // assists = null (0.0)
        // passSuccess = 5 (0.5 * 15% = 0.075)
        // aerialsWon = 5 (0.5 * 15% = 0.075)
        // motm = 5 (0.5 * 15% = 0.075)
        // Total = 0.15 + 0.075 + 0.075 + 0.075 = 0.375 -> rounds to 0.38
        PlayerSnapshot p1 = new PlayerSnapshot(3L, "Forward", 5, null, 5f, 5f, 5, 0, 0);

        // p2: goals = 5 (0.5 * 30% = 0.15)
        // assists = null (0.0)
        // passSuccess = 5 (0.5 * 15% = 0.075)
        // aerialsWon = 5 (0.5 * 15% = 0.075)
        // motm = 4.9 -> norm = 0.49 -> 0.49 * 15% = 0.0735
        // Total = 0.15 + 0.075 + 0.075 + 0.0735 = 0.3735 -> rounds to 0.37
        PlayerSnapshot p2 = new PlayerSnapshot(4L, "Forward", 5, null, 5f, 5f, 4, 0, 0);

        List<PlayerSnapshot> snapshot = List.of(pMin, pMax, p1, p2);

        BigDecimal val1 = calculator.calculate(p1, configA, snapshot, BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.75"), val1);

        // For p2: 0.15 + 0.075 + 0.075 + (4/10 * 0.15 = 0.06) = 0.36
        BigDecimal val2 = calculator.calculate(p2, configA, snapshot, BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.72"), val2);
    }

    @Test
    @DisplayName("Snapshot vacio o parametros nulos retornan 0.00")
    void casosBordeRetornanCero() {
        StrategyConfig configA = new StrategyConfig(StrategyType.ESTRATEGIA_A, weightsA);
        PlayerSnapshot p = new PlayerSnapshot(1L, "Forward", 10, 5, 80f, 3f, 2, 500, 10);

        assertEquals(new BigDecimal("0.00"), calculator.calculate(null, configA, List.of(p), BigDecimal.ZERO));
        assertEquals(new BigDecimal("0.00"), calculator.calculate(p, null, List.of(p), BigDecimal.ZERO));
        assertEquals(new BigDecimal("0.00"), calculator.calculate(p, configA, null, BigDecimal.ZERO));
        assertEquals(new BigDecimal("0.00"), calculator.calculate(p, configA, List.of(), BigDecimal.ZERO));
    }

    @Test
    @DisplayName("La valuacion acumula el valor base y aplica factor escala 2")
    void formulaAcumulativa() {
        StrategyConfig configA = new StrategyConfig(StrategyType.ESTRATEGIA_A, weightsA);
        PlayerSnapshot min = new PlayerSnapshot(1L, "Forward", 0, 0, 0f, 0f, 0, 0, 0);
        PlayerSnapshot max = new PlayerSnapshot(2L, "Forward", 10, 10, 10f, 10f, 10, 0, 0);
        PlayerSnapshot middle = new PlayerSnapshot(3L, "Forward", 5, 5, 5f, 5f, 5, 0, 0);

        assertEquals(new BigDecimal("1.00"), calculator.calculate(middle, configA,
                List.of(min, max), BigDecimal.ZERO));
        assertEquals(new BigDecimal("4.00"), calculator.calculate(middle, configA,
                List.of(min, max), new BigDecimal("3.00")));
    }

    @Test
    void valorBaseNegativoEsInvalido() {
        StrategyConfig configA = new StrategyConfig(StrategyType.ESTRATEGIA_A, weightsA);
        PlayerSnapshot player = new PlayerSnapshot(1L, "Forward", 0, 0, 0f, 0f, 0, 0, 0);

        org.junit.jupiter.api.Assertions.assertThrows(ValuationDomainException.class,
                () -> calculator.calculate(player, configA, List.of(player), new BigDecimal("-0.01")));
    }
}
