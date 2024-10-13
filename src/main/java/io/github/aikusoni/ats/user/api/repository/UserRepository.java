package io.github.aikusoni.ats.user.api.repository;

import io.github.aikusoni.ats.user.api.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    UserEntity findByUsername(String username);
    UserEntity deleteByUsername(String username);
}
