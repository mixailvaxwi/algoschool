package com.algoschool.step.repository;

import com.algoschool.step.entity.Step;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StepRepository extends JpaRepository<Step, Long> {
    List<Step> findAllByLessonId(Long lessonId, Sort sort);

    /** Id курса, которому принадлежит шаг — без подтягивания ленивых связей. */
    @Query("SELECT s.lesson.module.course.id FROM Step s WHERE s.id = :stepId")
    Optional<Long> findCourseIdByStepId(@Param("stepId") Long stepId);
}
