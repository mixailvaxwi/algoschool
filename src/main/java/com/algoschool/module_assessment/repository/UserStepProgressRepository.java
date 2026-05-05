package com.algoschool.module_assessment.repository;

import com.algoschool.module_assessment.entity.UserStepProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStepProgressRepository extends JpaRepository<UserStepProgress, Long> {

    Optional<UserStepProgress> findByUserIdAndStepId(Long userId, Long stepId);

    boolean existsByUserIdAndStepIdAndIsCompletedTrue(Long userId, Long stepId);

    // Находим все записи о прогрессе для юзера, где шаг завершен
    List<UserStepProgress> findAllByUserIdAndIsCompletedTrue(Long userId);
}