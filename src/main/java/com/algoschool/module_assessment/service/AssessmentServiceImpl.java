package com.algoschool.module_assessment.service;

import com.algoschool.module_assessment.dto.AssessmentResult;
import com.algoschool.module_assessment.entity.Step;
import com.algoschool.module_assessment.entity.UserStepProgress;
import com.algoschool.module_assessment.repository.StepRepository;
import com.algoschool.module_assessment.repository.UserStepProgressRepository;
import com.algoschool.module_user.entity.User;
import com.algoschool.module_user.repository.UserRepository;
import com.algoschool.module_assessment.service.checker.StepChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final StepRepository stepRepository;
    private final UserStepProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final List<StepChecker> checkers;

    @Override
    @Transactional
    public AssessmentResult submitSolution(Long userId, Long stepId, String studentAnswer) {

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Шаг не найден"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        StepChecker checker = checkers.stream()
                .filter(c -> c.supports(step))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Не найден чекер для шага типа " + step.getClass().getSimpleName()));

        boolean isCorrect = checker.check(step, studentAnswer);
        String message = isCorrect ? "Ответ верный!" : "Ответ неверный. Попробуйте еще раз!";

        Optional<UserStepProgress> existingProgress = progressRepository.findByUserIdAndStepId(userId, stepId);

        if (existingProgress.isPresent()) {
            UserStepProgress progress = existingProgress.get();

            // ИЗМЕНЕНО: используем isCompleted()
            if (progress.isCompleted()) {
                message = isCorrect ? "Верно! (Вы уже решили эту задачу ранее)" : "Ответ неверный, но задача уже была зачтена ранее.";
            }

            // ИЗМЕНЕНО: обновляем статус и дату
            progress.setCompleted(progress.isCompleted() || isCorrect);
            progress.setSubmittedPayload(studentAnswer);
            progress.setCompletedAt(LocalDateTime.now());

            progressRepository.save(progress);
        } else {
            // ИЗМЕНЕНО: при создании используем isCompleted
            UserStepProgress progress = UserStepProgress.builder()
                    .user(user)
                    .step(step)
                    .isCompleted(isCorrect)
                    .submittedPayload(studentAnswer)
                    .build();
            progressRepository.save(progress);
        }

        return new AssessmentResult(isCorrect, message);
    }
}