package com.algoschool.step.service;

import com.algoschool.exception.AppException;

import com.algoschool.step.entity.Step;
import com.algoschool.step.dto.StepCreateRequest;
import com.algoschool.step.dto.StepTeacherDto;
import com.algoschool.step.entity.*;
import com.algoschool.step.repository.StepRepository;
import com.algoschool.course.entity.Lesson;
import com.algoschool.course.repository.LessonRepository;
import com.algoschool.course.service.CourseAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StepServiceImpl implements StepService {
    private final LessonRepository lessonRepository;
    private final StepRepository stepRepository;
    private final CourseAccessService courseAccess;

    @Override
    @Transactional
    public StepTeacherDto addStepToLesson(Long lessonId, StepCreateRequest request, String username) {
        // Шаг можно добавить только в свой курс. Раньше проверки не было вовсе:
        // достаточно было знать lessonId, чтобы дописать шаг в чужой урок.
        courseAccess.requireAuthorOfLesson(lessonId, username);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> AppException.notFound("Урок не найден: " + lessonId));

        Step step = createEmptyStep(request.getStepType());
        applyRequestToStep(step, request);

        // ВАЖНО: устанавливаем обязательные поля
        step.setLesson(lesson);
        step.setOrderIndex(request.getOrderIndex());

        return toTeacherDto(stepRepository.save(step));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StepTeacherDto> getStepsForLesson(Long lessonId, String username) {
        courseAccess.requireAuthorOfLesson(lessonId, username);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> AppException.notFound("Урок не найден: " + lessonId));

        return lesson.getSteps().stream().map(this::toTeacherDto).toList();
    }

    @Override
    @Transactional
    public StepTeacherDto updateStep(Long stepId, StepCreateRequest request, String username) {
        courseAccess.requireAuthorOfStep(stepId, username);

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> AppException.notFound("Шаг не найден: " + stepId));

        // Тип шага завязан на конкретную таблицу (JOINED-наследование), поэтому
        // сменить его на лету нельзя — только удалить и создать заново.
        if (!typeNameOf(step).equals(request.getStepType())) {
            throw AppException.badRequest("Нельзя изменить тип шага при редактировании — удалите шаг и создайте новый");
        }

        applyRequestToStep(step, request);
        step.setOrderIndex(request.getOrderIndex());

        return toTeacherDto(stepRepository.save(step));
    }

    @Override
    @Transactional
    public void deleteStep(Long stepId, String username) {
        courseAccess.requireAuthorOfStep(stepId, username);

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> AppException.notFound("Шаг не найден: " + stepId));

        stepRepository.delete(step);
    }

    // --- Внутреннее -------------------------------------------------------

    private Step createEmptyStep(String stepType) {
        return switch (stepType) {
            case "THEORY" -> new TheoryStep();
            case "INPUT_PROBLEM" -> new TextProblem();
            case "CHOICE_PROBLEM" -> new ChoiceProblem();
            case "CODE_PROBLEM" -> new CodeProblem();
            default -> throw AppException.badRequest("Неизвестный тип шага: " + stepType);
        };
    }

    private void applyRequestToStep(Step step, StepCreateRequest request) {
        if (step instanceof TheoryStep theory) {
            theory.setContent(request.getContent());
        } else if (step instanceof TextProblem input) {
            input.setDescription(request.getDescription());
            input.setCorrectAnswer(request.getCorrectAnswer());
        } else if (step instanceof ChoiceProblem choice) {
            choice.setDescription(request.getDescription());
            choice.setOptions(request.getOptions());
            choice.setCorrectOptionIndex(request.getCorrectOptionIndex());
            choice.setMultipleChoice(request.getIsMultipleChoice() != null ? request.getIsMultipleChoice() : false);
        } else if (step instanceof CodeProblem code) {
            code.setDescription(request.getDescription());
            code.setTimeLimit(request.getTimeLimitSec());
            code.setMemoryLimit(request.getMemoryLimitMb());
            code.setAllowedLanguages(request.getAllowedLanguages());
            code.setEjudgeContestId(request.getEjudgeContestId());
            code.setEjudgeProblemId(request.getEjudgeProblemId());
        }
    }

    private String typeNameOf(Step step) {
        if (step instanceof TheoryStep) return "THEORY";
        if (step instanceof TextProblem) return "INPUT_PROBLEM";
        if (step instanceof ChoiceProblem) return "CHOICE_PROBLEM";
        if (step instanceof CodeProblem) return "CODE_PROBLEM";
        throw new IllegalStateException("Неизвестный тип шага: " + step.getClass().getSimpleName());
    }

    private StepTeacherDto toTeacherDto(Step step) {
        StepTeacherDto.StepTeacherDtoBuilder dto = StepTeacherDto.builder()
                .id(step.getId())
                .orderIndex(step.getOrderIndex())
                .stepType(typeNameOf(step));

        if (step instanceof TheoryStep theory) {
            dto.content(theory.getContent());
        } else if (step instanceof TextProblem input) {
            dto.description(input.getDescription()).correctAnswer(input.getCorrectAnswer());
        } else if (step instanceof ChoiceProblem choice) {
            dto.description(choice.getDescription())
                    .options(choice.getOptions())
                    .correctOptionIndex(choice.getCorrectOptionIndex())
                    .isMultipleChoice(choice.isMultipleChoice());
        } else if (step instanceof CodeProblem code) {
            dto.description(code.getDescription())
                    .timeLimitSec(code.getTimeLimit())
                    .memoryLimitMb(code.getMemoryLimit())
                    .allowedLanguages(code.getAllowedLanguages())
                    .ejudgeContestId(code.getEjudgeContestId())
                    .ejudgeProblemId(code.getEjudgeProblemId());
        }

        return dto.build();
    }
}
