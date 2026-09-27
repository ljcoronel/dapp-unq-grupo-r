package com.dappunq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^\\S+$", message = "El nombre no debe contener espacios")
    String nombre,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, max = 16, message = "La contraseña debe tener entre 4 y 16 caracteres")
    String password
) {
    public UserRequestDTO {
        nombre = nombre == null ? null : nombre.trim();
        password = password == null ? null : password.trim();
    }
}
