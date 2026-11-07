package com.dappunq.service.order;

import com.dappunq.dto.order.CrearOrdenRequest;
import com.dappunq.model.order.Orden;
import com.dappunq.model.order.TipoOperacionOrden;
import com.dappunq.persistence.order.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearOrdenCompraService {
    private final OrderAssetService assets;
    private final OrdenRepository orders;
    private final SuperuserLiquidityService superuserLiquidity;
    private final OrderMatchingService matching;

    public CrearOrdenCompraService(OrderAssetService assets, OrdenRepository orders,
                                   SuperuserLiquidityService superuserLiquidity,
                                   OrderMatchingService matching) {
        this.assets = assets;
        this.orders = orders;
        this.superuserLiquidity = superuserLiquidity;
        this.matching = matching;
    }

    @Transactional
    public Orden crear(CrearOrdenRequest request) {
        assets.bloquearJugador(request.playerId());
        assets.validarUsuario(request.usuarioId());
        assets.validarEstrategiaActiva(request.usuarioId());
        Orden orden = Orden.crear(request.usuarioId(), TipoOperacionOrden.COMPRA,
                request.playerId(), request.quantity(), request.price(), request.tolerance());
        assets.reservar(orden);
        orden = orders.save(orden);
        if (!superuserLiquidity.intentarLiquidezPrioritaria(orden)) {
            matching.intentarCoincidencia(orden);
        }
        return orders.findById(orden.getId()).orElseThrow();
    }
}
