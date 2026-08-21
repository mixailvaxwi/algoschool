package com.algoschool.course.service;

import com.algoschool.exception.AppException;

import com.algoschool.course.dto.player.*;
import com.algoschool.course.entity.Lesson;
import com.algoschool.course.repository.LessonRepository;
import com.algoschool.problem.entity.*;
import com.algoschool.step.entity.ProblemStep;
import com.algoschool.step.entity.Step;
import com.algoschool.step.entity.TheoryStep;
import com.algoschool.submission.repository.UserStepProgressRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
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

    /**
     * Плеерное DTO шага. Идентификатор — всегда идентификатор шага, а не
     * задачи: студент отправляет решение туда, где решает, а одна и та же
     * задача может стоять в нескольких уроках.
     */
    private StepPlayerDto mapToSafeDto(Step step) {
        if (step instanceof TheoryStep theory) {
            TheoryStepPlayerDto dto = new TheoryStepPlayerDto();
            dto.setId(theory.getId());
            dto.setOrderIndex(theory.getOrderIndex());
            dto.setContent(theory.getContent());
            return dto;
        }
        if (step instanceof ProblemStep problemStep) {
            return mapProblemToSafeDto(problemStep, problemStep.getProblem());
        }
        throw new IllegalArgumentException("Неизвестный тип шага: " + step.getClass().getSimpleName());
    }

    private StepPlayerDto mapProblemToSafeDto(ProblemStep step, Problem problem) {
        if (problem instanceof CodeProblem code) {
            CodeProblemPlayerDto dto = new CodeProblemPlayerDto();
            dto.setId(step.getId());
            dto.setOrderIndex(step.getOrderIndex());
            dto.setDescription(code.getDescription());
            dto.setTimeLimitSec(code.getTimeLimit());
            dto.setMemoryLimitMb(code.getMemoryLimit());
            dto.setAllowedLanguages(code.getAllowedLanguages());
            return dto;
        }
        if (problem instanceof TextProblem text) {
            TextProblemPlayerDto dto = new TextProblemPlayerDto();
            dto.setId(step.getId());
            dto.setOrderIndex(step.getOrderIndex());
            dto.setDescription(text.getDescription());
            return dto;
        }
        if (problem instanceof ChoiceProblem choice) {
            ChoiceProblemPlayerDto dto = new ChoiceProblemPlayerDto();
            dto.setId(step.getId());
            dto.setOrderIndex(step.getOrderIndex());
            dto.setDescription(choice.getDescription());
            dto.setOptions(choice.getOptions());
            dto.setIsMultipleChoice(choice.isMultipleChoice());
            return dto;
        }
        if (problem instanceof NumericProblem numeric) {
            NumericProblemPlayerDto dto = new NumericProblemPlayerDto();
            dto.setId(step.getId());
            dto.setOrderIndex(step.getOrderIndex());
            dto.setDescription(numeric.getDescription());
            // Допуск показываем: не зная требуемой точности, студент не
            // понимает, до скольких знаков округлять. Эталон, конечно, нет.
            dto.setTolerance(numeric.getTolerance());
            dto.setToleranceKind(numeric.getToleranceKind().name());
            return dto;
        }
        if (problem instanceof MatchingProblem matching) {
            MatchingProblemPlayerDto dto = new MatchingProblemPlayerDto();
            dto.setId(step.getId());
            dto.setOrderIndex(step.getOrderIndex());
            dto.setDescription(matching.getDescription());
            dto.setLeftItems(matching.getLeftItems());
            // Перемешанная правая колонка: в порядке хранения i-й правый
            // элемент подходит к i-му левому, то есть это готовый ответ.
            dto.setRightItems(matching.rightItemsForDisplay());
            return dto;
        }
        if (problem instanceof OrderingProblem ordering) {
            OrderingProblemPlayerDto dto = new OrderingProblemPlayerDto();
            dto.setId(step.getId());
            dto.setOrderIndex(step.getOrderIndex());
            dto.setDescription(ordering.getDescription());
            // Элементы хранятся в правильном порядке — показываем перемешанными.
            dto.setItems(ordering.itemsForDisplay());
            return dto;
        }
        if (problem instanceof OpenAnswerProblem open) {
            OpenAnswerProblemPlayerDto dto = new OpenAnswerProblemPlayerDto();
            dto.setId(step.getId());
            dto.setOrderIndex(step.getOrderIndex());
            dto.setDescription(open.getDescription());
            return dto;
        }
        throw new IllegalArgumentException("Неизвестный тип задачи: " + problem.getClass().getSimpleName());
    }
}