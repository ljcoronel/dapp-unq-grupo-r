package com.dappunq.service.order;

import com.dappunq.model.order.EstadoOrden;
import com.dappunq.model.order.Orden;
import com.dappunq.persistence.order.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultarOrdenesService {
    private final OrdenRepository orders;

    public ConsultarOrdenesService(OrdenRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public List<Orden> pendientes(Long usuarioId) {
        return orders.findByUsuarioYEstado(usuarioId, EstadoOrden.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<Orden> ejecutadas(Long usuarioId) {
        return orders.findEjecutadasByUsuario(usuarioId);
    }
}
