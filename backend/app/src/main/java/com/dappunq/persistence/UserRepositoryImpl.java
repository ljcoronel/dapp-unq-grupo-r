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
        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = jpaUserEntityRepository.save(entity);
        User savedUser = userMapper.toDomain(savedEntity);
        if (user != null && user.getId() == null && savedEntity.getId() != null) {
            user.setId(savedEntity.getId());
        }
        return savedUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserEntityRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByNombreIgnoreCase(String nombre) {
        return jpaUserEntityRepository.findByNombreIgnoreCase(nombre).map(userMapper::toDomain);
    }
}
