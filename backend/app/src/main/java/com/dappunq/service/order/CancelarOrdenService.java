package com.dappunq.service.order;

import com.dappunq.model.order.Orden;
import com.dappunq.model.order.exception.CodigoErrorOrden;
import com.dappunq.model.order.exception.OrdenException;
import com.dappunq.persistence.order.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CancelarOrdenService {
    private final OrdenRepository orders;
    private final OrderAssetService assets;

    public CancelarOrdenService(OrdenRepository orders, OrderAssetService assets) {
        this.orders = orders;
        this.assets = assets;
    }

    @Transactional
    public void cancelar(Long orderId, Long usuarioId) {
        Orden vista = orders.findById(orderId).orElseThrow(() ->
                new OrdenException(CodigoErrorOrden.ORDEN_NO_ENCONTRADA, "La orden no existe"));
        assets.bloquearJugador(vista.getJugadorId());
        Orden orden = orders.findByIdForUpdate(orderId).orElseThrow(() ->
                new OrdenException(CodigoErrorOrden.ORDEN_NO_ENCONTRADA, "La orden no existe"));
        orden.cancelar(usuarioId, Instant.now());
        assets.liberarReserva(orden);
        orders.save(orden);
    }
}
