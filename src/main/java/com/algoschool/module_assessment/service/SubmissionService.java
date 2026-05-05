package com.algoschool.module_assessment.service;

import com.algoschool.module_assessment.entity.*;
import com.algoschool.module_assessment.repository.StepRepository;
import com.algoschool.module_assessment.repository.UserStepProgressRepository;
import com.algoschool.module_user.entity.User;
import com.algoschool.module_user.repository.UserRepository;
import com.algoschool.module_assessment.service.checker.StepChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    // Spring магия: сюда автоматически подтянутся TheoryChecker, TextProblemChecker и т.д.
    private final List<StepChecker> checkers;
    private final StepRepository stepRepository;
    private final UserStepProgressRepository progressRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean submitAnswer(Long userId, Long stepId, String payload) {
        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Шаг не найден"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // 1. Ищем подходящий чекер
        StepChecker checker = checkers.stream()
                .filter(c -> c.supports(step))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Не найден чекер для шага типа " + step.getClass().getSimpleName()));

        // 2. Проверяем ответ
        boolean isCorrect = checker.check(step, payload);

        // 3. Записываем попытку в БД
        UserStepProgress progress = UserStepProgress.builder()
                .user(user)
                .step(step)
                .submittedPayload(payload)
                .isCompleted(isCorrect)
                .build();
        progressRepository.save(progress);

        // 4. Экономика: начисляем опыт, если ответ верный
        if (isCorrect) {
            // TODO: Проверить, не решал ли студент эту задачу раньше!
            // Если это первое успешное решение, помечаем:

        }

        return isCorrect;
    }
}