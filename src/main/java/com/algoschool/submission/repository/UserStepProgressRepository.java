package com.algoschool.submission.repository;

import com.algoschool.step.entity.Step;
import com.algoschool.submission.entity.UserStepProgress;
import com.algoschool.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- Обязательно добавьте этот импорт
import java.util.Optional;

@Repository
public interface UserStepProgressRepository extends JpaRepository<UserStepProgress, Long> {

    // Ищем прогресс конкретного студента по конкретному шагу (по сущностям)
    Optional<UserStepProgress> findByUserAndStep(User user, Step step);

    // --- ИСПРАВЛЕНИЕ ОШИБКИ: ДОБАВЛЯЕМ НОВЫЙ МЕТОД ---
    // Возвращает список всех успешно пройденных шагов для конкретного студента
    List<UserStepProgress> findAllByUserIdAndIsCompletedTrue(Long userId);
}