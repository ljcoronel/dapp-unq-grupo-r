package com.dappunq.unit; import com.dappunq.model.valuation.*; import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class PlayerValuationCalculatorTest { @Test void metricasEnumCerradas(){assertEquals(8,Metric.values().length); assertEquals(StrategyType.ESTRATEGIA_B,StrategyType.valueOf("ESTRATEGIA_B"));} }
