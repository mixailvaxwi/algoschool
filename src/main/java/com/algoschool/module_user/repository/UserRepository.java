package com.algoschool.module_user.repository;

import com.algoschool.module_user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data автоматически построит SQL-запрос `SELECT * FROM users WHERE email = ?`
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // Удобно для валидации при регистрации: занят ли email?
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}