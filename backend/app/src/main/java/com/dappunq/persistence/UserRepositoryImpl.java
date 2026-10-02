package com.dappunq.persistence;

import com.dappunq.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final JpaUserEntityRepository jpaUserEntityRepository;
    private final UserMapper userMapper;

    public UserRepositoryImpl(JpaUserEntityRepository jpaUserEntityRepository, UserMapper userMapper) {
        this.jpaUserEntityRepository = jpaUserEntityRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        UserEntity entity;
        if (user.getId() != null) {
            Optional<UserEntity> existingOpt = jpaUserEntityRepository.findById(user.getId());
            if (existingOpt.isPresent()) {
                entity = existingOpt.get();
                entity.setNombre(user.getNombre());
                entity.setPasswordHash(user.getPasswordHash());
            } else {
                entity = userMapper.toEntity(user);
            }
        } else {
            entity = userMapper.toEntity(user);
        }
        UserEntity savedEntity = jpaUserEntityRepository.save(entity);
        User savedUser = userMapper.toDomain(savedEntity);
        if (user.getId() == null && savedEntity.getId() != null) {
            user.setId(savedEntity.getId());
        }
        return savedUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpaUserEntityRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByNombreIgnoreCase(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Optional.empty();
        }
        return jpaUserEntityRepository.findByNombreIgnoreCase(nombre.trim()).map(userMapper::toDomain);
    }
}
