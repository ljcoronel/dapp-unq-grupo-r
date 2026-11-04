package com.dappunq.persistence.order;

import com.dappunq.model.order.EstadoOrden;
import com.dappunq.model.order.Orden;
import com.dappunq.model.order.TipoOperacionOrden;
import com.dappunq.model.order.exception.CodigoErrorOrden;
import com.dappunq.model.order.exception.OrdenException;
import com.dappunq.persistence.JpaUserEntityRepository;
import com.dappunq.persistence.PlayerRepository;
import com.dappunq.persistence.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrdenRepositoryImpl implements OrdenRepository {
    private final JpaOrdenEntityRepository ordenes;
    private final JpaUserEntityRepository usuarios;
    private final PlayerRepository jugadores;

    public OrdenRepositoryImpl(JpaOrdenEntityRepository ordenes,
                               JpaUserEntityRepository usuarios,
                               PlayerRepository jugadores) {
        this.ordenes = ordenes;
        this.usuarios = usuarios;
        this.jugadores = jugadores;
    }

    @Override
    public Orden save(Orden orden) {
        OrdenEntity entity;
        UserEntity comprador = findUser(orden.getCompradorId());
        UserEntity vendedor = findUser(orden.getVendedorId());
        if (orden.getId() == null) {
            var usuario = usuarios.findById(orden.getUsuarioId()).orElseThrow(() ->
                    new OrdenException(CodigoErrorOrden.ORDEN_NO_ENCONTRADA, "El usuario no existe"));
            var jugador = jugadores.findById(orden.getJugadorId()).orElseThrow(() ->
                    new OrdenException(CodigoErrorOrden.JUGADOR_NO_ENCONTRADO, "El jugador no existe"));
            entity = new OrdenEntity(orden, usuario, jugador);
        } else {
            entity = ordenes.findByIdBloqueada(orden.getId()).orElseThrow(() ->
                    new OrdenException(CodigoErrorOrden.ORDEN_NO_ENCONTRADA, "La orden no existe"));
            entity.actualizarDesde(orden, comprador, vendedor);
        }
        return toModel(ordenes.save(entity));
    }

    @Override
    public Optional<Orden> findById(Long id) {
        return ordenes.findById(id).map(this::toModel);
    }

    @Override
    public Optional<Orden> findByIdForUpdate(Long id) {
        return ordenes.findByIdBloqueada(id).map(this::toModel);
    }

    @Override
    public List<Orden> findContrariasPendientes(Orden orden) {
        TipoOperacionOrden tipoContrario = orden.getTipoOperacion() == TipoOperacionOrden.COMPRA
                ? TipoOperacionOrden.VENTA : TipoOperacionOrden.COMPRA;
        return ordenes.findContrariasBloqueadas(tipoContrario, orden.getJugadorId(),
                        orden.getCantidad(), EstadoOrden.PENDIENTE, orden.getUsuarioId())
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<Orden> findByUsuarioYEstado(Long usuarioId, EstadoOrden estado) {
        return ordenes.findAllByUsuarioIdAndEstadoOrderByFechaEmisionDescIdDesc(usuarioId, estado)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<Orden> findEjecutadasByUsuario(Long usuarioId) {
        return ordenes.findAllByUsuarioIdAndEstadoOrderByFechaEjecucionDescIdDesc(
                        usuarioId, EstadoOrden.EJECUTADA)
                .stream().map(this::toModel).toList();
    }

    private UserEntity findUser(Long id) {
        return id == null ? null : usuarios.findById(id).orElseThrow(() ->
                new OrdenException(CodigoErrorOrden.ORDEN_NO_ENCONTRADA, "El usuario no existe"));
    }

    private Orden toModel(OrdenEntity entity) {
        return Orden.rehidratar(entity.getId(), entity.getUsuario().getId(),
                entity.getTipoOperacion(), entity.getJugador().getId(), entity.getCantidad(),
                entity.getPrecio(), entity.getTolerancia(), entity.getEstado(), entity.getFechaEmision(),
                entity.getFechaEjecucion(), entity.getFechaCancelacion(), entity.getPrecioLiquidado(),
                entity.getComprador() == null ? null : entity.getComprador().getId(),
                entity.getVendedor() == null ? null : entity.getVendedor().getId());
    }
}
