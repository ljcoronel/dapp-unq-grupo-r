package com.dappunq.service.order;

import com.dappunq.model.order.Orden;
import com.dappunq.model.order.TipoOperacionOrden;
import com.dappunq.model.order.exception.CodigoErrorOrden;
import com.dappunq.model.order.exception.OrdenException;
import com.dappunq.persistence.order.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class OrderSettlementService {
    private final OrdenRepository orders;
    private final OrderAssetService assets;

    public OrderSettlementService(OrdenRepository orders, OrderAssetService assets) {
        this.orders = orders;
        this.assets = assets;
    }

    @Transactional
    public void liquidar(Orden base, Orden contraparte) {
        if (!base.compatibleCon(contraparte)) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "Las órdenes dejaron de ser compatibles");
        }
        Orden compra = base.getTipoOperacion() == TipoOperacionOrden.COMPRA ? base : contraparte;
        Orden venta = base.getTipoOperacion() == TipoOperacionOrden.VENTA ? base : contraparte;
        BigDecimal precio = Orden.precioDeLiquidacion(base, contraparte);
        Instant ahora = Instant.now();
        compra.ejecutar(compra.getUsuarioId(), venta.getUsuarioId(), precio, ahora);
        venta.ejecutar(compra.getUsuarioId(), venta.getUsuarioId(), precio, ahora);
        assets.liquidar(compra, venta, precio);
        orders.save(compra);
        orders.save(venta);
    }
}
