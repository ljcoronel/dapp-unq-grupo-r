package com.dappunq.persistence;

import com.dappunq.model.SnapshotEstadistico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SnapshotEstadisticoRepository extends JpaRepository<SnapshotEstadistico, Long> {
    List<SnapshotEstadistico> findByJugadorIdOrderByFechaSincronizacionDesc(String jugadorId);
}
