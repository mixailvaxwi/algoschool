package com.mpanyavin.algoschool.module_assessment.repository;

import com.mpanyavin.algoschool.module_assessment.entity.ProblemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemOptionRepository extends JpaRepository<ProblemOption, Long> {

    // Spring Data сам сгенерирует SQL:
    // SELECT * FROM problem_options WHERE problem_id = ? AND is_correct = true
    List<ProblemOption> findByProblemIdAndIsCorrectTrue(Long problemId);
}