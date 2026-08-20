package com.algoschool.course.service;

import com.algoschool.exception.AppException;

import com.algoschool.course.dto.player.*;
import com.algoschool.course.entity.Lesson;
import com.algoschool.course.repository.LessonRepository;
import com.algoschool.step.entity.*;
import com.algoschool.submission.repository.UserStepProgressRepository; // НОВЫЙ ИМПОРТ
import com.algoschool.user.entity.User; // НОВЫЙ ИМПОРТ
import com.algoschool.user.repository.UserRepository; // НОВЫЙ ИМПОРТ
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentLessonService {

    private final LessonRepository lessonRepository;
    private final UserStepProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final CourseAccessService courseAccess;

    @Transactional(readOnly = true)
    public LessonPlayerResponse getLessonForPlayer(Long courseId, Long lessonId, String username) {
        // Содержимое урока доступно только зачисленным (и автору курса).
        // Раньше проверялась лишь принадлежность урока курсу, поэтому любой
        // авторизованный пользователь читал платные и закрытые курсы целиком.
        courseAccess.requireEnrolled(courseId, username);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> AppException.notFound("Урок не найден"));

        if (!lesson.getModule().getCourse().getId().equals(courseId)) {
            throw AppException.notFound("Урок не принадлежит указанному курсу");
        }

        // 1. Мапим шаги в безопасные DTO (без ответов)
        List<StepPlayerDto> safeSteps = lesson.getSteps().stream()
                .map(this::mapToSafeDto)
                .collect(Collectors.toList());

        LessonPlayerDto lessonDto = LessonPlayerDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .orderIndex(lesson.getOrderIndex())
                .steps(safeSteps)
                .build();

        // 2. Получаем список ID пройденных шагов для этого пользователя
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));

        List<Long> completedStepIds = progressRepository.findAllByUserIdAndIsCompletedTrue(user.getId())
                .stream()
                .map(p -> p.getStep().getId())
                .toList();

        // 3. Возвращаем объединенный ответ
        return new LessonPlayerResponse(lessonDto, completedStepIds);
    }

    private StepPlayerDto mapToSafeDto(Step step) {
        if (step instanceof TheoryStep theory) {
            TheoryStepPlayerDto dto = new TheoryStepPlayerDto();
            dto.setId(theory.getId());
            dto.setOrderIndex(theory.getOrderIndex());
            dto.setContent(theory.getContent());
            return dto;
        } else if (step instanceof CodeProblem code) {
            CodeProblemPlayerDto dto = new CodeProblemPlayerDto();
            dto.setId(code.getId());
            dto.setOrderIndex(code.getOrderIndex());
            dto.setDescription(code.getDescription());
            dto.setTimeLimitSec(code.getTimeLimit());
            dto.setMemoryLimitMb(code.getMemoryLimit());
            dto.setAllowedLanguages(code.getAllowedLanguages());
            return dto;
        } else if (step instanceof TextProblem text) {
            TextProblemPlayerDto dto = new TextProblemPlayerDto();
            dto.setId(text.getId());
            dto.setOrderIndex(text.getOrderIndex());
            dto.setDescription(text.getDescription());
            return dto;
        } else if (step instanceof ChoiceProblem choice) {
            ChoiceProblemPlayerDto dto = new ChoiceProblemPlayerDto();
            dto.setId(choice.getId());
            dto.setOrderIndex(choice.getOrderIndex());
            dto.setDescription(choice.getDescription());
            dto.setOptions(choice.getOptions());
            dto.setIsMultipleChoice(choice.isMultipleChoice());
            return dto;
        }
        throw new IllegalArgumentException("Неизвестный тип шага: " + step.getClass().getSimpleName());
    }
}