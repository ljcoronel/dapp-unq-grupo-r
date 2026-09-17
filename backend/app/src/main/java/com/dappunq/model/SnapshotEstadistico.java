package com.dappunq.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "snapshot_estadistico")
public class SnapshotEstadistico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jugador_id", nullable = false)
    private String jugadorId;

    @Column(name = "fecha_sincronizacion", nullable = false)
    private OffsetDateTime fechaSincronizacion;

    // Store normalized payload as JSON string for flexibility
    @Column(name = "payload_estadistico", nullable = false, columnDefinition = "text")
    private String payloadEstadistico;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public SnapshotEstadistico() {}

    public SnapshotEstadistico(String jugadorId, OffsetDateTime fechaSincronizacion, String payloadEstadistico, String source) {
        setJugadorId(jugadorId);
        setFechaSincronizacion(fechaSincronizacion);
        setPayloadEstadistico(payloadEstadistico);
        setSource(source);
    }

    @PrePersist
    @PreUpdate
    public void updateTimestamps() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        if (jugadorId == null || jugadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("jugadorId es obligatorio");
        }
        this.jugadorId = jugadorId.trim();
    }

    public OffsetDateTime getFechaSincronizacion() {
        return fechaSincronizacion;
    }

    public void setFechaSincronizacion(OffsetDateTime fechaSincronizacion) {
        if (fechaSincronizacion == null) {
            throw new IllegalArgumentException("fechaSincronizacion es obligatoria");
        }
        this.fechaSincronizacion = fechaSincronizacion;
    }

    public String getPayloadEstadistico() {
        return payloadEstadistico;
    }

    public void setPayloadEstadistico(String payloadEstadistico) {
        if (payloadEstadistico == null) {
            throw new IllegalArgumentException("payloadEstadistico es obligatorio");
        }
        this.payloadEstadistico = payloadEstadistico;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? "" : source.trim();
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
