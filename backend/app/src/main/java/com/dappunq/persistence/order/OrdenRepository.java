package com.dappunq.persistence.order;

import com.dappunq.model.order.EstadoOrden;
import com.dappunq.model.order.Orden;

import java.util.List;
import java.util.Optional;

public interface OrdenRepository {
    Orden save(Orden orden);
    Optional<Orden> findById(Long id);
    Optional<Orden> findByIdForUpdate(Long id);
    List<Orden> findContrariasPendientes(Orden orden);
    List<Orden> findByUsuarioYEstado(Long usuarioId, EstadoOrden estado);
    List<Orden> findEjecutadasByUsuario(Long usuarioId);
}
