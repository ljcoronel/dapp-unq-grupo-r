package com.dappunq.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "creditos_disponibles", nullable = false, precision = 15, scale = 2,
            columnDefinition = "numeric(15,2) default 100.00")
    private BigDecimal creditosDisponibles = new BigDecimal("100.00");

    @Column(name = "creditos_bloqueados", nullable = false, precision = 15, scale = 2,
            columnDefinition = "numeric(15,2) default 0.00")
    private BigDecimal creditosBloqueados = new BigDecimal("0.00");

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected UserEntity() {
    }

    public UserEntity(Long id, String nombre, String passwordHash, LocalDateTime createdAt) {
        this.id = id;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public UserEntity(Long id, String nombre, String passwordHash) {
        this(id, nombre, passwordHash, null);
    }

    public UserEntity(String nombre, String passwordHash) {
        this(null, nombre, passwordHash, null);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getCreditosDisponibles() {
        return creditosDisponibles;
    }

    public BigDecimal getCreditosBloqueados() {
        return creditosBloqueados;
    }

    public void setCreditosDisponibles(BigDecimal creditosDisponibles) {
        if (creditosDisponibles == null || creditosDisponibles.signum() < 0) {
            throw new IllegalArgumentException("Los créditos disponibles no pueden ser negativos");
        }
        this.creditosDisponibles = creditosDisponibles.setScale(2);
    }

    public void actualizarSaldoCreditos(BigDecimal disponibles, BigDecimal bloqueados) {
        this.creditosDisponibles = disponibles;
        this.creditosBloqueados = bloqueados;
    }
}
