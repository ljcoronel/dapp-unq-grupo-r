package com.dappunq.service.order;

import com.dappunq.dto.order.CrearOrdenRequest;
import com.dappunq.model.order.Orden;
import com.dappunq.model.order.TipoOperacionOrden;
import com.dappunq.persistence.order.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearOrdenVentaService {
    private final OrderAssetService assets;
    private final OrdenRepository orders;
    private final OrderMatchingService matching;

    public CrearOrdenVentaService(OrderAssetService assets, OrdenRepository orders,
                                  OrderMatchingService matching) {
        this.assets = assets;
        this.orders = orders;
        this.matching = matching;
    }

    @Transactional
    public Orden crear(CrearOrdenRequest request) {
        assets.bloquearJugador(request.playerId());
        assets.validarUsuario(request.usuarioId());
        assets.validarEstrategiaActiva(request.usuarioId());
        Orden orden = Orden.crear(request.usuarioId(), TipoOperacionOrden.VENTA,
                request.playerId(), request.quantity(), request.price(), request.tolerance());
        assets.reservar(orden);
        orden = orders.save(orden);
        matching.intentarCoincidencia(orden);
        return orders.findById(orden.getId()).orElseThrow();
    }
}
