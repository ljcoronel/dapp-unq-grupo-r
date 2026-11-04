package com.dappunq.persistence.order;

import com.dappunq.model.order.EstadoOrden;
import com.dappunq.model.order.TipoOperacionOrden;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaOrdenEntityRepository extends JpaRepository<OrdenEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrdenEntity o where o.tipoOperacion = :tipo and o.jugador.id = :jugadorId "
            + "and o.cantidad = :cantidad and o.estado = :estado and o.usuario.id <> :usuarioId "
            + "order by o.fechaEmision asc, o.id asc")
    List<OrdenEntity> findContrariasBloqueadas(@Param("tipo") TipoOperacionOrden tipo,
                                               @Param("jugadorId") Long jugadorId,
                                               @Param("cantidad") Integer cantidad,
                                               @Param("estado") EstadoOrden estado,
                                               @Param("usuarioId") Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrdenEntity o where o.id = :id")
    Optional<OrdenEntity> findByIdBloqueada(@Param("id") Long id);

    List<OrdenEntity> findAllByUsuarioIdAndEstadoOrderByFechaEmisionDescIdDesc(
            Long usuarioId, EstadoOrden estado);

    List<OrdenEntity> findAllByUsuarioIdAndEstadoOrderByFechaEjecucionDescIdDesc(
            Long usuarioId, EstadoOrden estado);
}
