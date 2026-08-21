package com.algoschool.step.service;

import com.algoschool.course.entity.Course;
import com.algoschool.course.entity.Lesson;
import com.algoschool.course.repository.LessonRepository;
import com.algoschool.course.service.CourseAccessService;
import com.algoschool.exception.AppException;
import com.algoschool.grade.service.GradeService;
import com.algoschool.problem.dto.ProblemDto;
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
    private final GradeService gradeService;

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

        Step saved = stepRepository.save(step);
        if (saved instanceof ProblemStep problemStep) {
            // Каждая задача урока — столбец журнала оценок. Заводим его здесь,
            // а не при первом решении: преподаватель должен видеть пустой
            // столбец сразу, иначе журнал курса неполон до первой отправки.
            gradeService.onProblemStepCreated(problemStep);
        }

        return toTeacherDto(saved, username);
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

        // Столбец журнала осмыслен ровно столько же, сколько само размещение.
        gradeService.onProblemStepRemoved(stepId);

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
            problem = problemService.createEntity(problemMapper.fromStepRequest(request, type), course.getAuthor());
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

        problemMapper.apply(problem, problemMapper.fromStepRequest(request, ProblemType.of(problem)));
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

    /**
     * Содержание задачи вытаскивает ProblemContentMapper — тот же, что отдаёт
     * карточку банка. Дублировать разбор подтипов здесь значило бы завести два
     * места, которые обязаны знать про все семь типов задач и неизбежно
     * разъедутся при добавлении восьмого.
     */
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
        long usageCount = knownUsage.containsKey(problem.getId())
                ? knownUsage.get(problem.getId())
                : problemStepRepository.countByProblemId(problem.getId());

        // Правильные ответы нужны форме редактирования, но показывать чужой
        // ответ незачем: править задачу всё равно нельзя.
        ProblemDto content = problemMapper.toDto(problem, usageCount, editable, editable);

        return dto.problemId(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .difficulty(content.getDifficulty())
                .visibility(content.getVisibility())
                .maxScore(content.getMaxScore())
                .tags(content.getTags())
                .problemEditable(editable)
                .problemUsageCount(usageCount)
                .options(content.getOptions())
                .correctOptionIndexes(content.getCorrectOptionIndexes())
                .isMultipleChoice(content.getIsMultipleChoice())
                .correctAnswer(content.getCorrectAnswer())
                .correctValue(content.getCorrectValue())
                .tolerance(content.getTolerance())
                .toleranceKind(content.getToleranceKind())
                .leftItems(content.getLeftItems())
                .rightItems(content.getRightItems())
                .orderedItems(content.getOrderedItems())
                .reviewGuidelines(content.getReviewGuidelines())
                .timeLimitSec(content.getTimeLimitSec())
                .memoryLimitMb(content.getMemoryLimitMb())
                .allowedLanguages(content.getAllowedLanguages())
                .ejudgeContestId(content.getEjudgeContestId())
                .ejudgeProblemId(content.getEjudgeProblemId())
                .build();
    }
}
