package com.algoschool.step.service;

import com.algoschool.course.entity.Course;
import com.algoschool.course.entity.Lesson;
import com.algoschool.course.repository.LessonRepository;
import com.algoschool.course.service.CourseAccessService;
import com.algoschool.exception.AppException;
import com.algoschool.problem.dto.ProblemRequest;
import com.algoschool.problem.entity.*;
import com.algoschool.problem.service.ProblemContentMapper;
import com.algoschool.problem.service.ProblemService;
import com.algoschool.step.dto.StepCreateRequest;
import com.algoschool.step.dto.StepTeacherDto;
import com.algoschool.step.entity.ProblemStep;
import com.algoschool.step.entity.Step;
import com.algoschool.step.entity.TheoryStep;
import com.algoschool.step.repository.ProblemStepRepository;
import com.algoschool.step.repository.StepRepository;
import com.algoschool.submission.repository.UserStepProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StepServiceImpl implements StepService {

    private static final String THEORY = "THEORY";

    private final LessonRepository lessonRepository;
    private final StepRepository stepRepository;
    private final ProblemStepRepository problemStepRepository;
    private final UserStepProgressRepository progressRepository;
    private final CourseAccessService courseAccess;
    private final ProblemService problemService;
    private final ProblemContentMapper problemMapper;

    @Override
    @Transactional
    public StepTeacherDto addStepToLesson(Long lessonId, StepCreateRequest request, String username) {
        // Шаг можно добавить только в свой курс. Раньше проверки не было вовсе:
        // достаточно было знать lessonId, чтобы дописать шаг в чужой урок.
        Course course = courseAccess.requireAuthorOfLesson(lessonId, username);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> AppException.notFound("Урок не найден: " + lessonId));

        Step step = THEORY.equals(request.getStepType())
                ? newTheoryStep(request)
                : newProblemStep(request, course, username);

        step.setLesson(lesson);
        step.setOrderIndex(request.getOrderIndex());

        return toTeacherDto(stepRepository.save(step), username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StepTeacherDto> getStepsForLesson(Long lessonId, String username) {
        courseAccess.requireAuthorOfLesson(lessonId, username);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> AppException.notFound("Урок не найден: " + lessonId));

        // Число размещений считаем одним запросом на весь урок: по запросу на
        // шаг список из двадцати задач стоил бы двадцати обращений к базе.
        List<Long> problemIds = lesson.getSteps().stream()
                .filter(ProblemStep.class::isInstance)
                .map(step -> ((ProblemStep) step).getProblem().getId())
                .toList();
        Map<Long, Long> usage = problemIds.isEmpty()
                ? Map.of()
                : problemStepRepository.countUsages(problemIds).stream()
                        .collect(Collectors.toMap(
                                ProblemStepRepository.ProblemUsage::getProblemId,
                                ProblemStepRepository.ProblemUsage::getUsageCount));

        return lesson.getSteps().stream()
                .map(step -> toTeacherDto(step, username, usage))
                .toList();
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

        if (step instanceof TheoryStep theory) {
            theory.setContent(requireContent(request));
        } else if (step instanceof ProblemStep problemStep) {
            updatePlacedProblem(problemStep, request, username);
        }

        step.setOrderIndex(request.getOrderIndex());

        return toTeacherDto(stepRepository.save(step), username);
    }

    @Override
    @Transactional
    public void deleteStep(Long stepId, String username) {
        courseAccess.requireAuthorOfStep(stepId, username);

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> AppException.notFound("Шаг не найден: " + stepId));

        // Прогресс осмыслен только вместе с шагом, к которому относится, —
        // без этой строки внешний ключ user_step_progress не дал бы снять
        // задачу с урока, а снятие теперь рядовое действие: сама задача
        // остаётся в банке. Отправленные решения не трогаем: они привязаны к
        // задаче, а ссылка на снятое размещение обнулится (ON DELETE SET NULL).
        progressRepository.deleteByStepId(stepId);

        stepRepository.delete(step);
    }

    // --- Создание -----------------------------------------------------------

    private TheoryStep newTheoryStep(StepCreateRequest request) {
        TheoryStep theory = new TheoryStep();
        theory.setContent(requireContent(request));
        return theory;
    }

    private ProblemStep newProblemStep(StepCreateRequest request, Course course, String username) {
        ProblemType type;
        try {
            type = ProblemType.valueOf(request.getStepType());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw AppException.badRequest("Неизвестный тип шага: " + request.getStepType());
        }

        Problem problem;
        if (request.getProblemId() != null) {
            // Вставка готовой задачи из банка: своей любой, чужой — публичной.
            problem = problemService.requirePlaceable(request.getProblemId(), username);
            if (ProblemType.of(problem) != type) {
                throw AppException.badRequest("Тип шага не совпадает с типом выбранной задачи");
            }
        } else {
            // Форма редактора урока не знает про банк — задача заводится попутно
            // и достаётся автору курса.
            problem = problemService.createEntity(toProblemRequest(request, type), course.getAuthor());
        }

        ProblemStep step = new ProblemStep();
        step.setProblem(problem);
        return step;
    }

    // --- Правка -------------------------------------------------------------

    private void updatePlacedProblem(ProblemStep step, StepCreateRequest request, String username) {
        Problem problem = step.getProblem();

        if (request.getProblemId() != null && !request.getProblemId().equals(problem.getId())) {
            // Подменить задачу под уже существующим шагом — значит оставить
            // прогресс и решения студентов привязанными к прежней задаче под
            // видом новой. Пусть преподаватель удалит шаг и поставит другой.
            throw AppException.badRequest(
                    "Нельзя подменить задачу у существующего шага — удалите шаг и поставьте другую задачу");
        }

        if (request.getDescription() == null) {
            // Запрос без условия задачи меняет только сам шаг — его позицию в
            // уроке. Позиция принадлежит уроку, а не задаче, поэтому переставить
            // можно и чужую публичную задачу, поставленную в свой урок.
            return;
        }

        if (!isOwnedBy(problem, username)) {
            throw AppException.forbidden(
                    "Задача из банка другого преподавателя — её содержание отсюда не изменить. "
                            + "Можно поменять только позицию шага в уроке.");
        }

        problemMapper.apply(problem, toProblemRequest(request, ProblemType.of(problem)));
    }

    private ProblemRequest toProblemRequest(StepCreateRequest request, ProblemType type) {
        ProblemRequest problemRequest = new ProblemRequest();
        problemRequest.setProblemType(type.name());
        problemRequest.setTitle(problemMapper.requireTitle(request.getTitle(), request.getDescription()));
        problemRequest.setDescription(request.getDescription());
        problemRequest.setDifficulty(request.getDifficulty());
        problemRequest.setVisibility(request.getVisibility());
        problemRequest.setMaxScore(request.getMaxScore());
        problemRequest.setTags(request.getTags());
        problemRequest.setOptions(request.getOptions());
        problemRequest.setCorrectOptionIndexes(request.getCorrectOptionIndexes());
        problemRequest.setIsMultipleChoice(request.getIsMultipleChoice());
        problemRequest.setCorrectAnswer(request.getCorrectAnswer());
        problemRequest.setTimeLimitSec(request.getTimeLimitSec());
        problemRequest.setMemoryLimitMb(request.getMemoryLimitMb());
        problemRequest.setAllowedLanguages(request.getAllowedLanguages());
        problemRequest.setEjudgeContestId(request.getEjudgeContestId());
        problemRequest.setEjudgeProblemId(request.getEjudgeProblemId());
        return problemRequest;
    }

    // --- Внутреннее ---------------------------------------------------------

    private String requireContent(StepCreateRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw AppException.badRequest("Текст теории обязателен");
        }
        return request.getContent();
    }

    private boolean isOwnedBy(Problem problem, String username) {
        return problem.getAuthor().getUsername().equals(username);
    }

    private String typeNameOf(Step step) {
        if (step instanceof TheoryStep) return THEORY;
        if (step instanceof ProblemStep problemStep) return ProblemType.of(problemStep.getProblem()).name();
        throw new IllegalStateException("Неизвестный тип шага: " + step.getClass().getSimpleName());
    }

    private StepTeacherDto toTeacherDto(Step step, String username) {
        return toTeacherDto(step, username, Map.of());
    }

    private StepTeacherDto toTeacherDto(Step step, String username, Map<Long, Long> knownUsage) {
        StepTeacherDto.StepTeacherDtoBuilder dto = StepTeacherDto.builder()
                .id(step.getId())
                .orderIndex(step.getOrderIndex())
                .stepType(typeNameOf(step));

        if (step instanceof TheoryStep theory) {
            return dto.content(theory.getContent()).build();
        }

        Problem problem = ((ProblemStep) step).getProblem();
        boolean editable = isOwnedBy(problem, username);

        dto.problemId(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty() == null ? null : problem.getDifficulty().name())
                .visibility(problem.getVisibility().name())
                .maxScore(problem.getMaxScore())
                .tags(List.copyOf(problem.getTags()))
                .problemEditable(editable)
                .problemUsageCount(knownUsage.containsKey(problem.getId())
                        ? knownUsage.get(problem.getId())
                        : problemStepRepository.countByProblemId(problem.getId()));

        // Правильные ответы нужны форме редактирования, но показывать чужой
        // ответ незачем: править задачу всё равно нельзя.
        if (problem instanceof ChoiceProblem choice) {
            dto.options(List.copyOf(choice.getOptions()))
                    .isMultipleChoice(choice.isMultipleChoice());
            if (editable) {
                dto.correctOptionIndexes(choice.getCorrectOptionIndexes().stream().sorted().toList());
            }
        } else if (problem instanceof TextProblem text) {
            if (editable) {
                dto.correctAnswer(text.getCorrectAnswer());
            }
        } else if (problem instanceof CodeProblem code) {
            dto.timeLimitSec(code.getTimeLimit())
                    .memoryLimitMb(code.getMemoryLimit())
                    .allowedLanguages(code.getAllowedLanguages())
                    .ejudgeContestId(code.getEjudgeContestId())
                    .ejudgeProblemId(code.getEjudgeProblemId());
        }

        return dto.build();
    }
}
