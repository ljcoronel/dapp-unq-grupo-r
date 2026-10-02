package com.dappunq.persistence;

import com.dappunq.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(entity.getId(), entity.getNombre(), entity.getPasswordHash());
    }

    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserEntity(user.getId(), user.getNombre(), user.getPasswordHash());
    }
}
