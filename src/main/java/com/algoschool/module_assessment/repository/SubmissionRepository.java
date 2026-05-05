package com.algoschool.module_assessment.repository;

import com.algoschool.module_assessment.entity.Submission;
import com.algoschool.module_assessment.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // Получить всю историю попыток конкретного студента по конкретной задаче
    // Сортировка по убыванию даты (от новых к старым)
    List<Submission> findAllByUserIdAndProblemIdOrderByCreatedAtDesc(Long userId, Long problemId);

    // Проверка: отправлял ли студент вообще хоть какие-то решения по этой задаче?
    // Если возвращает false — значит это его самая первая попытка (нужно увеличить attemptedStudentsCount)
    boolean existsByUserIdAndProblemId(Long userId, Long problemId);

    // Проверка: есть ли у студента хотя бы одно решение с определенным статусом?
    // Полезно, чтобы узнать, решил ли он уже эту задачу (CORRECT)
    boolean existsByUserIdAndProblemIdAndStatus(Long userId, Long problemId, SubmissionStatus status);
}