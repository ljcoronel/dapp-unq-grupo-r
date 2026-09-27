package com.dappunq.service;

import com.dappunq.dto.UserRequestDTO;
import com.dappunq.dto.UserResponseDTO;
import com.dappunq.exception.InvalidCredentialsException;
import com.dappunq.exception.UserAlreadyExistsException;
import com.dappunq.model.User;
import com.dappunq.persistence.UserRepository;
import com.dappunq.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResult login(UserRequestDTO request) {
        String nombre = normalizeName(request.nombre());
        User user = userRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResult(new UserResponseDTO(user.getNombre()), jwtService.generateToken(user));
    }

    @Transactional
    public UserResponseDTO register(UserRequestDTO request) {
        String nombre = normalizeName(request.nombre());
        if (userRepository.findByNombreIgnoreCase(nombre).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        User user = new User(nombre, passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return new UserResponseDTO(user.getNombre());
    }

    private String normalizeName(String nombre) {
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String normalized = nombre.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        return normalized;
    }

    public record AuthResult(UserResponseDTO user, String token) {
    }
}
