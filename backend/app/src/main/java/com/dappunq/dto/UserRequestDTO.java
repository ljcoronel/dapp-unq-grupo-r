package com.dappunq.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRequestDTO(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "La contraseña es obligatoria") String password
) {
    public UserRequestDTO {
        nombre = nombre == null ? null : nombre.trim();
        password = password == null ? null : password.trim();
    }
}
