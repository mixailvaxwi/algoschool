package com.algoschool.module_assessment.repository;

import com.algoschool.module_assessment.entity.Step;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StepRepository extends JpaRepository<Step, Long> {
    List<Step> findAllByLessonId(Long lessonId, Sort sort);
}
