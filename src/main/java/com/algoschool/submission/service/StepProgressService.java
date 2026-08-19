package com.algoschool.submission.service;

import com.algoschool.exception.AppException;

import com.algoschool.course.service.CourseAccessService;
import com.algoschool.step.entity.Step;
import com.algoschool.step.entity.TheoryStep;
import com.algoschool.step.repository.StepRepository;
import com.algoschool.submission.entity.UserStepProgress;
import com.algoschool.submission.repository.UserStepProgressRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StepProgressService {

    private final UserStepProgressRepository progressRepository;
    private final StepRepository stepRepository;
    private final UserRepository userRepository;
    private final CourseAccessService courseAccess;

    @Transactional
    public void markTheoryAsCompleted(Long stepId, String username) {
        // Отмечать прогресс можно только по курсу, на который записан.
        courseAccess.requireEnrolledForStep(stepId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> AppException.notFound("Шаг не найден"));

        // Защита: этот метод можно вызвать только для теории
        if (!(step instanceof TheoryStep)) {
            throw AppException.badRequest("Этот эндпоинт предназначен только для теоретических шагов. Для задач используйте эндпоинт /submit");
        }

        // Ищем существующий прогресс или создаем новый
        UserStepProgress progress = progressRepository.findByUserAndStep(user, step)
                .orElse(UserStepProgress.builder()
                        .user(user)
                        .step(step)
                        .build());

        // Ставим галочку "Пройдено"
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        progressRepository.save(progress);
    }
}