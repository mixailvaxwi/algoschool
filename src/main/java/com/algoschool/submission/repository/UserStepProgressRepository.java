package com.algoschool.submission.repository;

import com.algoschool.step.entity.Step;
import com.algoschool.submission.entity.UserStepProgress;
import com.algoschool.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStepProgressRepository extends JpaRepository<UserStepProgress, Long> {

    // Ищем прогресс конкретного студента по конкретному шагу (по сущностям)
    Optional<UserStepProgress> findByUserAndStep(User user, Step step);

    // Возвращает список всех успешно пройденных шагов для конкретного студента
    List<UserStepProgress> findAllByUserIdAndIsCompletedTrue(Long userId);

    /** Прогресс всех студентов по шагу — нужен, когда шаг снимают с урока. */
    @Modifying
    @Query("DELETE FROM UserStepProgress p WHERE p.step.id = :stepId")
    void deleteByStepId(@Param("stepId") Long stepId);
}
