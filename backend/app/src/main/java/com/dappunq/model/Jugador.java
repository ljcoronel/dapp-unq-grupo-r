package com.dappunq.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

@Entity
@Table(name = "jugadores", uniqueConstraints = @UniqueConstraint(columnNames = "id"))
public class Jugador {
    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String liga;

    @Column(nullable = false)
    private String equipo;

    @Column(nullable = false)
    private String posicion;

    @Column(nullable = false)
    private Integer pases = 0;

    @Column(nullable = false)
    private Integer tiros = 0;

    @Column(nullable = false)
    private Integer intercepciones = 0;

    @Column(nullable = false)
    private Integer calificaciones = 0;

    @Column(nullable = false)
    private Integer minutosJugados = 0;

    @Column(nullable = false)
    private Integer goles = 0;

    @Column(nullable = false)
    private Integer asistencias = 0;

    @Column(nullable = false)
    private Integer tirosAlArco = 0;

    @Column(nullable = false)
    private Integer pasesRealizados = 0;

    @Column(nullable = false)
    private Integer tarjetasAmarillas = 0;

    @Column(nullable = false)
    private Integer tarjetasRojas = 0;

    @Column(nullable = false)
    private Integer ratingGeneral = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Jugador() {}

    public Jugador(String id, String nombre, String liga, String equipo, String posicion) {
        setId(id);
        setNombre(nombre);
        setLiga(liga);
        setEquipo(equipo);
        setPosicion(posicion);
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

    public String getId() { return id; }
    public void setId(String id) {
        String sanitized = normalizeText(id);
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("El id del jugador es obligatorio");
        }
        this.id = sanitized;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        String sanitized = normalizeText(nombre);
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("El nombre del jugador es obligatorio");
        }
        this.nombre = sanitized;
    }

    public String getLiga() { return liga; }
    public void setLiga(String liga) {
        String sanitized = normalizeText(liga);
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("La liga del jugador es obligatoria");
        }
        this.liga = sanitized;
    }

    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) {
        String sanitized = normalizeText(equipo);
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("El equipo del jugador es obligatorio");
        }
        this.equipo = sanitized;
    }

    public String getPosicion() { return posicion; }
    public void setPosicion(String posicion) {
        String sanitized = normalizeText(posicion);
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("La posición del jugador es obligatoria");
        }
        this.posicion = sanitized;
    }

    public Integer getPases() { return pases; }
    public void setPases(Integer pases) { this.pases = pases == null ? 0 : pases; }

    public Integer getTiros() { return tiros; }
    public void setTiros(Integer tiros) { this.tiros = tiros == null ? 0 : tiros; }

    public Integer getIntercepciones() { return intercepciones; }
    public void setIntercepciones(Integer intercepciones) { this.intercepciones = intercepciones == null ? 0 : intercepciones; }

    public Integer getCalificaciones() { return calificaciones; }
    public void setCalificaciones(Integer calificaciones) { this.calificaciones = calificaciones == null ? 0 : calificaciones; }

    public Integer getMinutosJugados() { return minutosJugados; }
    public void setMinutosJugados(Integer minutosJugados) { this.minutosJugados = minutosJugados == null ? 0 : minutosJugados; }

    public Integer getGoles() { return goles; }
    public void setGoles(Integer goles) { this.goles = goles == null ? 0 : goles; }

    public Integer getAsistencias() { return asistencias; }
    public void setAsistencias(Integer asistencias) { this.asistencias = asistencias == null ? 0 : asistencias; }

    public Integer getTirosAlArco() { return tirosAlArco; }
    public void setTirosAlArco(Integer tirosAlArco) { this.tirosAlArco = tirosAlArco == null ? 0 : tirosAlArco; }

    public Integer getPasesRealizados() { return pasesRealizados; }
    public void setPasesRealizados(Integer pasesRealizados) { this.pasesRealizados = pasesRealizados == null ? 0 : pasesRealizados; }

    public Integer getTarjetasAmarillas() { return tarjetasAmarillas; }
    public void setTarjetasAmarillas(Integer tarjetasAmarillas) { this.tarjetasAmarillas = tarjetasAmarillas == null ? 0 : tarjetasAmarillas; }

    public Integer getTarjetasRojas() { return tarjetasRojas; }
    public void setTarjetasRojas(Integer tarjetasRojas) { this.tarjetasRojas = tarjetasRojas == null ? 0 : tarjetasRojas; }

    public Integer getRatingGeneral() { return ratingGeneral; }
    public void setRatingGeneral(Integer ratingGeneral) { this.ratingGeneral = ratingGeneral == null ? 0 : ratingGeneral; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    private static String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
