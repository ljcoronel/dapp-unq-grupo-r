package com.dappunq.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class ApiKey {
    private String permission;
    private String keyHash;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApiKey() {
    }

    public ApiKey(String permission, String keyHash, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        setPermission(permission);
        setKeyHash(keyHash);
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ApiKey(String permission, String keyHash, boolean active) {
        this(permission, keyHash, active, null, null);
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            throw new IllegalArgumentException("El permiso de la API key es obligatorio");
        }
        String normalized = permission.trim();
        if (!"scraping".equals(normalized)) {
            throw new IllegalArgumentException("El permiso debe ser 'scraping'");
        }
        this.permission = normalized;
    }

    public String getKeyHash() {
        return keyHash;
    }

    public void setKeyHash(String keyHash) {
        if (keyHash == null || keyHash.isBlank()) {
            throw new IllegalArgumentException("El hash de la API key es obligatorio");
        }
        this.keyHash = keyHash.trim();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiKey apiKey = (ApiKey) o;
        return active == apiKey.active && Objects.equals(permission, apiKey.permission) && Objects.equals(keyHash, apiKey.keyHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permission, keyHash, active);
    }
}
