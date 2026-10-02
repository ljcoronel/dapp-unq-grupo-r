package com.dappunq.model;

import com.dappunq.exception.InvalidUserDataException;

import java.util.Objects;

public class User {
    private Long id;
    private String nombre;
    private String passwordHash;

    public User(Long id, String nombre, String passwordHash) {
        setId(id);
        setNombre(nombre);
        setPasswordHash(passwordHash);
    }

    public User(String nombre, String passwordHash) {
        this(null, nombre, passwordHash);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (id != null && id <= 0) {
            throw new InvalidUserDataException("El identificador del usuario debe ser positivo");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null) {
            throw new InvalidUserDataException("El nombre es obligatorio");
        }
        String sanitized = nombre.trim();
        if (sanitized.isBlank()) {
            throw new InvalidUserDataException("El nombre es obligatorio");
        }
        this.nombre = sanitized;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isBlank()) {
            throw new InvalidUserDataException("La contraseña es obligatoria");
        }
        this.passwordHash = passwordHash.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(nombre, user.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}
