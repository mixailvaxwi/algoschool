package com.algoschool.module_assessment.repository;

import com.algoschool.module_assessment.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    // Атомарное увеличение счетчика попыток на стороне базы данных
    @Modifying
    @Transactional
    @Query("UPDATE Problem p SET p.attemptedStudentsCount = p.attemptedStudentsCount + 1 WHERE p.id = :problemId")
    void incrementAttemptedCount(@Param("problemId") Long problemId);

    // Атомарное увеличение счетчика успешных решений
    @Modifying
    @Transactional
    @Query("UPDATE Problem p SET p.successStudentsCount = p.successStudentsCount + 1 WHERE p.id = :problemId")
    void incrementSuccessCount(@Param("problemId") Long problemId);
}