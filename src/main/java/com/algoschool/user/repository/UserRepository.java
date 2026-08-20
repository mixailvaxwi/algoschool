package com.algoschool.user.repository;

import com.algoschool.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data автоматически построит SQL-запрос `SELECT * FROM users WHERE email = ?`
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // Удобно для валидации при регистрации: занят ли email?
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // Поиск для админ-панели (UC-A-01): по логину, email или имени, без учёта регистра.
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
            String username, String email, String name);
}