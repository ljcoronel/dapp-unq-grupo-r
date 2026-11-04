package com.dappunq.integration.order;

import com.dappunq.dto.order.CrearOrdenRequest;
import com.dappunq.model.order.EstadoOrden;
import com.dappunq.model.order.exception.OrdenException;
import com.dappunq.model.order.TipoOperacionOrden;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import com.dappunq.persistence.ApiKeyRepository;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.LeagueEntity;
import com.dappunq.persistence.LeagueRepository;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.TeamEntity;
import com.dappunq.persistence.TeamRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.ValuationHistoryRepository;
import com.dappunq.persistence.ValuationPersistenceMapper;
import com.dappunq.persistence.ValuationRepository;
import com.dappunq.persistence.order.JpaOrdenEntityRepository;
import com.dappunq.persistence.order.OrdenEntity;
import com.dappunq.service.WhoScoredScrapingService;
import com.dappunq.service.order.CancelarOrdenService;
import com.dappunq.service.order.ConsultarOrdenesService;
import com.dappunq.service.order.CrearOrdenCompraService;
import com.dappunq.service.order.CrearOrdenVentaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OrderMarketIntegrationTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @MockitoBean
    private WhoScoredScrapingService scrapingService;

    @Autowired private CrearOrdenCompraService buys;
    @Autowired private CrearOrdenVentaService sells;
    @Autowired private CancelarOrdenService cancellations;
    @Autowired private ConsultarOrdenesService queries;
    @Autowired private JpaUserEntityRepository users;
    @Autowired private StrategyConfigRepository strategies;
    @Autowired private PlayerRepository players;
    @Autowired private TeamRepository teams;
    @Autowired private LeagueRepository leagues;
    @Autowired private HoldingRepository holdings;
    @Autowired private JpaOrdenEntityRepository orders;
    @Autowired private ValuationRepository valuations;
    @Autowired private ValuationHistoryRepository valuationHistory;
    @Autowired private ApiKeyRepository apiKeys;
    @Autowired private ValuationPersistenceMapper valuationMapper;

    private UserEntity buyer;
    private UserEntity seller;
    private PlayerEntity player;

    @BeforeEach
    void setUp() {
        clean();
        buyer = userWithStrategy("buyer");
        seller = userWithStrategy("seller");
        player = createPlayer();
        holdings.save(new HoldingEntity(seller, player, 10, new BigDecimal("5.00")));
    }

    @AfterEach
    void cleanUp() {
        clean();
    }

    @Test
    void compraYVentaReservanLiquidarCreditosTokensYValuanTenenciaNueva() {
        var pendingBuy = buys.crear(request(buyer, 3, "20.00", "2.00"));
        assertEquals(EstadoOrden.PENDIENTE, pendingBuy.getEstado());
        assertEquals(0, user(buyer.getId()).getCreditosDisponibles().compareTo(new BigDecimal("78.00")));
        assertEquals(0, user(buyer.getId()).getCreditosBloqueados().compareTo(new BigDecimal("22.00")));

        var executedSell = sells.crear(request(seller, 3, "21.00", "0.00"));
        assertEquals(EstadoOrden.EJECUTADA, executedSell.getEstado());
        assertEquals(0, executedSell.getPrecioLiquidado().compareTo(new BigDecimal("21.00")));
        assertEquals(0, user(buyer.getId()).getCreditosDisponibles().compareTo(new BigDecimal("79.00")));
        assertEquals(BigDecimal.ZERO.setScale(2), user(buyer.getId()).getCreditosBloqueados());
        assertEquals(0, user(seller.getId()).getCreditosDisponibles().compareTo(new BigDecimal("121.00")));

        HoldingEntity buyerHolding = holdings.findByUserIdAndPlayerId(buyer.getId(), player.getId()).orElseThrow();
        HoldingEntity sellerHolding = holdings.findByUserIdAndPlayerId(seller.getId(), player.getId()).orElseThrow();
        assertEquals(3, buyerHolding.getTokens());
        assertNotEquals(new BigDecimal("21.00"), buyerHolding.getValorUnitario());
        assertEquals(7, sellerHolding.getTokens());
        assertEquals(0, sellerHolding.getTokensBloqueados());
    }

    @Test
    void rechazaOrdenSinEstrategiaSinReservarCreditos() {
        UserEntity userWithoutStrategy = users.save(new UserEntity("no_strategy", "password"));
        assertThrows(OrdenException.class,
                () -> buys.crear(request(userWithoutStrategy, 1, "10.00", "0.00")));
        UserEntity reloaded = user(userWithoutStrategy.getId());
        assertEquals(0, reloaded.getCreditosDisponibles().compareTo(new BigDecimal("100.00")));
        assertEquals(BigDecimal.ZERO.setScale(2), reloaded.getCreditosBloqueados());
    }

    @Test
    void rechazaCompraSinFondosConPrecisionInvalidaOCantidadExcesivaSinReservasParciales() {
        buyer.setCreditosDisponibles(new BigDecimal("20.99"));
        users.save(buyer);
        assertThrows(OrdenException.class, () -> buys.crear(request(buyer, 1, "20.00", "1.00")));
        assertThrows(OrdenException.class, () -> buys.crear(request(buyer, 1, "1.001", "0.00")));
        assertThrows(OrdenException.class, () -> buys.crear(request(buyer, 101, "1.00", "0.00")));

        UserEntity reloaded = user(buyer.getId());
        assertEquals(0, reloaded.getCreditosDisponibles().compareTo(new BigDecimal("20.99")));
        assertEquals(BigDecimal.ZERO.setScale(2), reloaded.getCreditosBloqueados());
        assertTrue(orders.findAll().isEmpty());
    }

    @Test
    void ventaRechazaTokensInsuficientesYExigeEstrategiaSinBloqueoParcial() {
        assertThrows(OrdenException.class, () -> sells.crear(request(seller, 11, "10.00", "0.00")));
        assertEquals(0, holdings.findByUserIdAndPlayerId(seller.getId(), player.getId())
                .orElseThrow().getTokensBloqueados());

        sells.crear(request(seller, 7, "10.00", "0.00"));
        assertThrows(OrdenException.class, () -> sells.crear(request(seller, 4, "10.00", "0.00")));
        assertThrows(OrdenException.class, () -> sells.crear(request(seller, 0, "10.00", "0.00")));
        assertThrows(OrdenException.class, () -> sells.crear(request(seller, 1, "1.001", "0.00")));
        assertEquals(7, holdings.findByUserIdAndPlayerId(seller.getId(), player.getId())
                .orElseThrow().getTokensBloqueados());

        UserEntity withoutStrategy = users.save(new UserEntity("seller_without_strategy", "password"));
        holdings.save(new HoldingEntity(withoutStrategy, player, 5));
        assertThrows(OrdenException.class, () -> sells.crear(request(withoutStrategy, 1, "10.00", "0.00")));
        assertEquals(0, holdings.findByUserIdAndPlayerId(withoutStrategy.getId(), player.getId())
                .orElseThrow().getTokensBloqueados());
    }

    @Test
    void soloUnaCompraConcurrenteConsumeLaVentaCompleta() throws Exception {
        UserEntity buyerTwo = userWithStrategy("buyer_two");
        sells.crear(request(seller, 2, "10.00", "0.00"));
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> {
                await(start);
                return buys.crear(request(buyer, 2, "10.00", "0.00"));
            });
            Future<?> second = executor.submit(() -> {
                await(start);
                return buys.crear(request(buyerTwo, 2, "10.00", "0.00"));
            });
            start.countDown();
            first.get();
            second.get();
        } finally {
            executor.shutdownNow();
        }

        List<OrdenEntity> saved = orders.findAll();
        assertEquals(1, saved.stream().filter(order -> order.getEstado() == EstadoOrden.EJECUTADA
                && order.getTipoOperacion() == TipoOperacionOrden.VENTA).count());
        assertEquals(1, saved.stream().filter(order -> order.getEstado() == EstadoOrden.EJECUTADA
                && order.getTipoOperacion() == TipoOperacionOrden.COMPRA).count());
        assertEquals(1, saved.stream().filter(order -> order.getEstado() == EstadoOrden.PENDIENTE
                && order.getTipoOperacion() == TipoOperacionOrden.COMPRA).count());
        assertEquals(8, holdings.findByUserIdAndPlayerId(seller.getId(), player.getId())
                .orElseThrow().getTokens());
    }

    @Test
    void superuserLiquidezTienePrioridadSiHayTokensLibres() {
        UserEntity superuser = userWithStrategy("superuser");
        holdings.save(new HoldingEntity(superuser, player, 5));

        var executedBuy = buys.crear(request(buyer, 2, "15.00", "1.00"));

        assertEquals(EstadoOrden.EJECUTADA, executedBuy.getEstado());
        assertEquals(2, orders.findAll().size());
        assertEquals(3, holdings.findByUserIdAndPlayerId(superuser.getId(), player.getId())
                .orElseThrow().getTokens());
    }

    @Test
    void superuserSinInventarioSuficienteNoCreaVentaYNoSeAutoCoincide() {
        UserEntity superuser = userWithStrategy("superuser");
        holdings.save(new HoldingEntity(superuser, player, 1));

        var pendingBuy = buys.crear(request(buyer, 2, "15.00", "1.00"));
        assertEquals(EstadoOrden.PENDIENTE, pendingBuy.getEstado());
        assertEquals(1, orders.findAll().size());

        var pendingSale = sells.crear(request(superuser, 1, "15.00", "0.00"));
        assertEquals(EstadoOrden.PENDIENTE, pendingSale.getEstado());
        var superuserBuy = buys.crear(request(superuser, 1, "15.00", "0.00"));
        assertEquals(EstadoOrden.PENDIENTE, superuserBuy.getEstado());
        assertEquals(EstadoOrden.PENDIENTE, orders.findById(pendingSale.getId()).orElseThrow().getEstado());
    }

    @Test
    void consultasFiltranPorPropietarioYCancelarLiberaLaReservaCompleta() {
        var pendingBuy = buys.crear(request(buyer, 1, "10.00", "2.00"));
        assertEquals(1, queries.pendientes(buyer.getId()).size());
        assertTrue(queries.pendientes(seller.getId()).isEmpty());
        assertThrows(OrdenException.class,
                () -> cancellations.cancelar(pendingBuy.getId(), seller.getId()));
        cancellations.cancelar(pendingBuy.getId(), buyer.getId());
        assertTrue(queries.pendientes(buyer.getId()).isEmpty());
        assertEquals(0, user(buyer.getId()).getCreditosDisponibles()
                .compareTo(new BigDecimal("100.00")));

        sells.crear(request(seller, 2, "10.00", "0.00"));
        buys.crear(request(buyer, 2, "10.00", "0.00"));
        assertEquals(1, queries.ejecutadas(buyer.getId()).size());
        assertEquals(1, queries.ejecutadas(seller.getId()).size());
        assertTrue(queries.ejecutadas(buyerTwoId()).isEmpty());
    }

    private Long buyerTwoId() {
        return userWithStrategy("unrelated_user").getId();
    }

    private CrearOrdenRequest request(UserEntity owner, int quantity, String price, String tolerance) {
        return new CrearOrdenRequest(owner.getId(), player.getId(), quantity,
                new BigDecimal(price), new BigDecimal(tolerance));
    }

    private UserEntity userWithStrategy(String name) {
        UserEntity user = users.save(new UserEntity(name, "password"));
        Map<String, BigDecimal> weights = new HashMap<>();
        weights.put("goals", BigDecimal.valueOf(30));
        weights.put("assists", BigDecimal.valueOf(25));
        weights.put("passSuccessPercentage", BigDecimal.valueOf(15));
        weights.put("aerialsWon", BigDecimal.valueOf(15));
        weights.put("timesManOfTheMatch", BigDecimal.valueOf(15));
        strategies.save(new StrategyConfigEntity(user, StrategyType.ESTRATEGIA_A,
                valuationMapper.write(weights), StrategyStatus.ACTUAL, Instant.now()));
        return user;
    }

    private PlayerEntity createPlayer() {
        TeamEntity team = teams.save(new TeamEntity(1L, "Order Team"));
        LeagueEntity league = leagues.save(new LeagueEntity(1L, "Order League", "Test"));
        return players.save(new PlayerEntity(1L, "Order Player", team, league, 25, 180, 75,
                "Defender", 10, 900, 2, 1, 0, 0, 1.5f, 85f, 3f, 1));
    }

    private UserEntity user(Long id) {
        return users.findById(id).orElseThrow();
    }

    private void clean() {
        orders.deleteAll();
        valuationHistory.deleteAll();
        valuations.deleteAll();
        holdings.deleteAll();
        strategies.deleteAll();
        apiKeys.deleteAll();
        players.deleteAll();
        teams.deleteAll();
        leagues.deleteAll();
        users.deleteAll();
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrumpido mientras se iniciaba la operación concurrente", exception);
        }
    }
}
