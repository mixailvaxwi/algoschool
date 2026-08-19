package com.algoschool.submission.repository;

import com.algoschool.submission.entity.Submission;
import com.algoschool.submission.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long>, JpaSpecificationExecutor<Submission> {

    List<Submission> findAllByUserIdAndProblemIdOrderByCreatedAtDesc(Long userId, Long problemId);

    boolean existsByUserIdAndProblemId(Long userId, Long problemId);

    @Query("SELECT s FROM Submission s JOIN FETCH s.problem WHERE s.status = :status")
    List<Submission> findByStatusWithProblem(@Param("status") SubmissionStatus status);
}