package com.dappunq.service.order;

import com.dappunq.model.order.Orden;
import com.dappunq.model.order.TipoOperacionOrden;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.order.OrdenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SuperuserLiquidityService {
    private final JpaUserEntityRepository users;
    private final OrderAssetService assets;
    private final OrdenRepository orders;
    private final OrderSettlementService settlement;

    @Value("${valuation.superuser.username:superuser}")
    private String superuserName;

    public SuperuserLiquidityService(JpaUserEntityRepository users,
                                     OrderAssetService assets,
                                     OrdenRepository orders,
                                     OrderSettlementService settlement) {
        this.users = users;
        this.assets = assets;
        this.orders = orders;
        this.settlement = settlement;
    }

    @Transactional
    public boolean intentarLiquidezPrioritaria(Orden compra) {
        var superuser = users.findByNombreIgnoreCase(superuserName).orElse(null);
        if (superuser == null || superuser.getId().equals(compra.getUsuarioId())
                || !assets.tieneEstrategiaActiva(superuser.getId())
                || !assets.tieneTokensLibres(superuser.getId(), compra.getJugadorId(), compra.getCantidad())) {
            return false;
        }
        Orden venta = Orden.crear(superuser.getId(), TipoOperacionOrden.VENTA,
                compra.getJugadorId(), compra.getCantidad(), compra.getPrecio(), BigDecimal.ZERO);
        assets.reservar(venta);
        venta = orders.save(venta);
        settlement.liquidar(compra, venta);
        return true;
    }
}
