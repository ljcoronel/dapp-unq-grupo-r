package com.dappunq.service.order;

import com.dappunq.model.order.Orden;
import com.dappunq.model.order.SaldoCreditos;
import com.dappunq.model.order.TenenciaTokens;
import com.dappunq.model.order.TipoOperacionOrden;
import com.dappunq.model.order.exception.CodigoErrorOrden;
import com.dappunq.model.order.exception.OrdenException;
import com.dappunq.model.valuation.PlayerSnapshot;
import com.dappunq.model.valuation.PlayerValuationCalculator;
import com.dappunq.model.valuation.StrategyConfig;
import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.HoldingRepository;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.StrategyConfigEntity;
import com.dappunq.persistence.StrategyConfigRepository;
import com.dappunq.persistence.UserEntity;
import com.dappunq.persistence.ValuationPersistenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OrderAssetService {
    private final JpaUserEntityRepository users;
    private final PlayerRepository players;
    private final HoldingRepository holdings;
    private final StrategyConfigRepository strategies;
    private final ValuationPersistenceMapper valuationMapper;
    private final PlayerValuationCalculator calculator = new PlayerValuationCalculator();

    public OrderAssetService(JpaUserEntityRepository users,
                             PlayerRepository players,
                             HoldingRepository holdings,
                             StrategyConfigRepository strategies,
                             ValuationPersistenceMapper valuationMapper) {
        this.users = users;
        this.players = players;
        this.holdings = holdings;
        this.strategies = strategies;
        this.valuationMapper = valuationMapper;
    }

    @Transactional
    public PlayerEntity bloquearJugador(Long jugadorId) {
        return players.findByIdForUpdate(jugadorId).orElseThrow(() ->
                new OrdenException(CodigoErrorOrden.JUGADOR_NO_ENCONTRADO, "El jugador no existe"));
    }

    @Transactional
    public void validarUsuario(Long usuarioId) {
        usuarioBloqueado(usuarioId);
    }

    @Transactional
    public void validarEstrategiaActiva(Long usuarioId) {
        if (!tieneEstrategiaActiva(usuarioId)) {
            throw new OrdenException(CodigoErrorOrden.ESTRATEGIA_INACTIVA,
                    "El usuario debe tener una Estrategia de Valuación activa");
        }
    }

    @Transactional(readOnly = true)
    public boolean tieneEstrategiaActiva(Long usuarioId) {
        return strategies.findFirstByUserIdAndEstado(usuarioId, StrategyStatus.ACTUAL).isPresent();
    }

    @Transactional
    public void reservar(Orden orden) {
        UserEntity usuario = usuarioBloqueado(orden.getUsuarioId());
        if (orden.getTipoOperacion() == TipoOperacionOrden.COMPRA) {
            SaldoCreditos saldo = saldo(usuario);
            saldo.reservar(orden.getPrecio().add(orden.getTolerancia()));
            actualizarSaldo(usuario, saldo);
            return;
        }
        HoldingEntity holding = tenenciaBloqueada(usuario.getId(), orden.getJugadorId());
        TenenciaTokens tenencia = tokens(holding);
        tenencia.reservar(orden.getCantidad());
        actualizarTenencia(holding, tenencia);
    }

    @Transactional
    public void liberarReserva(Orden orden) {
        if (orden.getTipoOperacion() == TipoOperacionOrden.COMPRA) {
            UserEntity usuario = usuarioBloqueado(orden.getUsuarioId());
            SaldoCreditos saldo = saldo(usuario);
            saldo.liberar(orden.getPrecio().add(orden.getTolerancia()));
            actualizarSaldo(usuario, saldo);
            return;
        }
        HoldingEntity holding = tenenciaBloqueada(orden.getUsuarioId(), orden.getJugadorId());
        TenenciaTokens tenencia = tokens(holding);
        tenencia.liberar(orden.getCantidad());
        actualizarTenencia(holding, tenencia);
    }

    @Transactional
    public void liquidar(Orden compra, Orden venta, BigDecimal precioLiquidado) {
        Long firstUserId = compra.getUsuarioId().compareTo(venta.getUsuarioId()) < 0
                ? compra.getUsuarioId() : venta.getUsuarioId();
        Long secondUserId = compra.getUsuarioId().compareTo(venta.getUsuarioId()) < 0
                ? venta.getUsuarioId() : compra.getUsuarioId();
        UserEntity firstUser = usuarioBloqueado(firstUserId);
        UserEntity secondUser = usuarioBloqueado(secondUserId);
        UserEntity comprador = compra.getUsuarioId().equals(firstUserId) ? firstUser : secondUser;
        UserEntity vendedor = venta.getUsuarioId().equals(firstUserId) ? firstUser : secondUser;
        SaldoCreditos saldoComprador = saldo(comprador);
        saldoComprador.liquidarCompra(compra.getPrecio().add(compra.getTolerancia()), precioLiquidado);
        actualizarSaldo(comprador, saldoComprador);
        SaldoCreditos saldoVendedor = saldo(vendedor);
        saldoVendedor.acreditar(precioLiquidado);
        actualizarSaldo(vendedor, saldoVendedor);

        HoldingEntity tenenciaVendedor = tenenciaBloqueada(vendedor.getId(), compra.getJugadorId());
        TenenciaTokens tokensVendedor = tokens(tenenciaVendedor);
        tokensVendedor.transferirBloqueados(compra.getCantidad());
        actualizarTenencia(tenenciaVendedor, tokensVendedor);
        var tenenciaExistente = holdings.findByUserIdAndPlayerIdForUpdate(
                comprador.getId(), compra.getJugadorId());
        HoldingEntity tenenciaComprador;
        if (tenenciaExistente.isPresent()) {
            tenenciaComprador = tenenciaExistente.get();
            TenenciaTokens tokensComprador = tokens(tenenciaComprador);
            tokensComprador.recibir(compra.getCantidad());
            actualizarTenencia(tenenciaComprador, tokensComprador);
        } else {
            tenenciaComprador = crearTenencia(comprador, compra.getJugadorId(), compra.getCantidad());
        }
        holdings.save(tenenciaComprador);
    }

    @Transactional
    public boolean tieneTokensLibres(Long usuarioId, Long jugadorId, int cantidad) {
        return holdings.findByUserIdAndPlayerIdForUpdate(usuarioId, jugadorId)
                .map(holding -> tokens(holding).disponibles() >= cantidad)
                .orElse(false);
    }

    private SaldoCreditos saldo(UserEntity user) {
        return new SaldoCreditos(user.getCreditosDisponibles(), user.getCreditosBloqueados());
    }

    private void actualizarSaldo(UserEntity user, SaldoCreditos saldo) {
        user.actualizarSaldoCreditos(saldo.getDisponibles(), saldo.getBloqueados());
    }

    private TenenciaTokens tokens(HoldingEntity holding) {
        return new TenenciaTokens(holding.getTokens(), holding.getTokensBloqueados());
    }

    private void actualizarTenencia(HoldingEntity holding, TenenciaTokens tokens) {
        holding.actualizarTokens(tokens.getCantidad(), tokens.getBloqueados());
    }

    private UserEntity usuarioBloqueado(Long id) {
        return users.findByIdForUpdate(id).orElseThrow(() ->
                new OrdenException(CodigoErrorOrden.ORDEN_NO_ENCONTRADA, "El usuario no existe"));
    }

    private HoldingEntity tenenciaBloqueada(Long usuarioId, Long jugadorId) {
        return holdings.findByUserIdAndPlayerIdForUpdate(usuarioId, jugadorId).orElseThrow(() ->
                new OrdenException(CodigoErrorOrden.RECURSOS_INSUFICIENTES,
                        "El usuario no tiene tokens libres suficientes"));
    }

    private HoldingEntity crearTenencia(UserEntity comprador, Long jugadorId, int cantidad) {
        PlayerEntity player = players.findById(jugadorId).orElseThrow(() ->
                new OrdenException(CodigoErrorOrden.JUGADOR_NO_ENCONTRADO, "El jugador no existe"));
        StrategyConfigEntity active = strategies.findFirstByUserIdAndEstado(
                        comprador.getId(), StrategyStatus.ACTUAL)
                .orElseThrow(() -> new OrdenException(CodigoErrorOrden.ESTRATEGIA_INACTIVA,
                        "El comprador debe tener una Estrategia de Valuación activa"));
        List<PlayerSnapshot> snapshots = players.findAll().stream()
                .map(valuationMapper::toSnapshot).toList();
        PlayerSnapshot target = valuationMapper.toSnapshot(player);
        Map<String, BigDecimal> weights = valuationMapper.read(active.getPesos());
        StrategyConfig config = new StrategyConfig(active.getEstrategia(), weights);
        BigDecimal unitValue = calculator.calculate(target, config, snapshots, BigDecimal.ZERO);
        return new HoldingEntity(comprador, player, cantidad, unitValue);
    }
}
