package com.dappunq.persistence;

import com.dappunq.model.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByNombreIgnoreCase(String nombre);
}
