package com.algoschool.grade.service;

import com.algoschool.course.entity.Course;
import com.algoschool.course.entity.Lesson;
import com.algoschool.course.repository.UserCourseRepository;
import com.algoschool.course.service.CourseAccessService;
import com.algoschool.exception.AppException;
import com.algoschool.grade.dto.*;
import com.algoschool.grade.entity.Grade;
import com.algoschool.grade.entity.GradeItem;
import com.algoschool.grade.entity.GradeItemKind;
import com.algoschool.grade.entity.GradePolicy;
import com.algoschool.grade.repository.GradeItemRepository;
import com.algoschool.grade.repository.GradeRepository;
import com.algoschool.problem.entity.Problem;
import com.algoschool.step.entity.ProblemStep;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeItemRepository gradeItemRepository;
    private final GradeRepository gradeRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserRepository userRepository;
    private final CourseAccessService courseAccess;
    private final GradeCalculator calculator;

    // --- Сопровождение элементов журнала ----------------------------------

    @Override
    @Transactional
    public void onProblemStepCreated(ProblemStep step) {
        Course course = step.getLesson().getModule().getCourse();
        Problem problem = step.getProblem();

        GradeItem item = new GradeItem();
        item.setCourse(course);
        item.setKind(GradeItemKind.PROBLEM);
        item.setStep(step);
        item.setTitle(problem.getTitle());
        // Вес по умолчанию — вес задачи в банке. Дальше он живёт своей жизнью:
        // одна и та же задача может стоить в разных курсах по-разному.
        item.setMaxScore(problem.getMaxScore());
        item.setPolicy(GradePolicy.BEST);
        item.setOrderIndex(gradeItemRepository.findMaxOrderIndex(course.getId()) + 1);

        gradeItemRepository.save(item);
    }

    @Override
    @Transactional
    public void onProblemStepRemoved(Long stepId) {
        gradeItemRepository.findByStepId(stepId).ifPresent(item -> {
            gradeRepository.deleteByGradeItemId(item.getId());
            gradeItemRepository.delete(item);
        });
    }

    // --- Пересчёт ----------------------------------------------------------

    @Override
    @Transactional
    public void recomputeForProblem(User user, Problem problem) {
        calculator.recompute(user, problem.getId(),
                gradeItemRepository.findByProblemForEnrolledUser(problem.getId(), user.getId()));
    }

    @Override
    @Transactional
    public void recomputeCourseForUser(Long courseId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));

        calculator.recomputeAll(user, gradeItemRepository.findAllByCourseIdOrderByOrderIndexAsc(courseId));
    }

    // --- Журнал преподавателя ---------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public GradebookDto gradebook(Long courseId, String teacherUsername) {
        Course course = courseAccess.requireAuthor(courseId, teacherUsername);

        List<GradeItem> items = gradeItemRepository.findAllByCourseIdOrderByOrderIndexAsc(courseId);
        int totalMaxScore = items.stream().mapToInt(GradeItem::getMaxScore).sum();

        // Все оценки курса одним запросом: строка на студента и запрос на клетку
        // превратили бы журнал из тридцати человек в сотни обращений к базе.
        Map<Long, Map<Long, Grade>> byUser = gradeRepository.findAllByCourseId(courseId).stream()
                .collect(Collectors.groupingBy(
                        grade -> grade.getUser().getId(),
                        Collectors.toMap(grade -> grade.getGradeItem().getId(), grade -> grade)));

        List<GradebookRowDto> rows = userCourseRepository.findAllByCourseId(courseId).stream()
                .map(enrollment -> toRow(enrollment.getUser(), byUser, totalMaxScore))
                .sorted(Comparator.comparing(GradebookRowDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new GradebookDto(
                courseId,
                course.getTitle(),
                items.stream().map(this::toItemDto).toList(),
                rows,
                totalMaxScore);
    }

    @Override
    @Transactional(readOnly = true)
    public String exportGradebookCsv(Long courseId, String teacherUsername) {
        GradebookDto gradebook = gradebook(courseId, teacherUsername);

        StringBuilder csv = new StringBuilder();
        // Разделитель — точка с запятой: Excel в русской локали открывает CSV с
        // запятыми одной колонкой. BOM в начале — чтобы он же не показал
        // кириллицу кракозябрами.
        csv.append('﻿');

        List<String> header = new ArrayList<>(List.of("Логин", "Имя"));
        gradebook.items().forEach(item -> header.add(item.title() + " (" + item.maxScore() + ")"));
        header.add("Итого (" + gradebook.totalMaxScore() + ")");
        header.add("Процент");
        appendRow(csv, header);

        for (GradebookRowDto row : gradebook.rows()) {
            List<String> cells = new ArrayList<>(List.of(row.username(), row.name()));
            for (GradeItemDto item : gradebook.items()) {
                GradeCellDto cell = row.scores().get(item.id());
                cells.add(cell == null ? "" : String.valueOf(cell.score()));
            }
            cells.add(String.valueOf(row.totalScore()));
            cells.add(String.format(Locale.ROOT, "%.1f", row.percent()));
            appendRow(csv, cells);
        }

        return csv.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeItemDto> items(Long courseId, String teacherUsername) {
        courseAccess.requireAuthor(courseId, teacherUsername);
        return gradeItemRepository.findAllByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(this::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public GradeItemDto createManualItem(Long courseId, GradeItemRequest request, String teacherUsername) {
        Course course = courseAccess.requireAuthor(courseId, teacherUsername);

        GradeItem item = new GradeItem();
        item.setCourse(course);
        item.setKind(GradeItemKind.MANUAL);
        item.setTitle(request.getTitle());
        item.setMaxScore(request.getMaxScore());
        item.setPolicy(GradePolicy.BEST); // у ручного элемента политика не используется
        item.setOrderIndex(gradeItemRepository.findMaxOrderIndex(courseId) + 1);

        return toItemDto(gradeItemRepository.save(item));
    }

    @Override
    @Transactional
    public GradeItemDto updateItem(Long itemId, GradeItemRequest request, String teacherUsername) {
        GradeItem item = requireOwnedItem(itemId, teacherUsername);

        item.setTitle(request.getTitle());
        item.setMaxScore(request.getMaxScore());

        if (item.getKind() == GradeItemKind.PROBLEM) {
            if (request.getPolicy() != null) {
                item.setPolicy(GradePolicy.parse(request.getPolicy()));
            }
            gradeItemRepository.save(item);

            // И вес, и политика меняют уже выставленные баллы, поэтому
            // пересчитываем всегда: иначе журнал показывал бы оценки по старому
            // правилу до тех пор, пока студенты не отправят что-нибудь ещё.
            recomputeItemForAllStudents(item);
        } else {
            // У ручного элемента баллы проставлены человеком — смена веса их не
            // пересчитывает, но и превышать новый вес они не должны.
            gradeItemRepository.save(item);
            capManualGrades(item);
        }

        return toItemDto(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId, String teacherUsername) {
        GradeItem item = requireOwnedItem(itemId, teacherUsername);
        if (item.getKind() != GradeItemKind.MANUAL) {
            throw AppException.conflict(
                    "Столбец задачи снимается вместе с самой задачей — удалите шаг из урока");
        }
        gradeRepository.deleteByGradeItemId(itemId);
        gradeItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void setManualGrade(GradeUpdateRequest request, String teacherUsername) {
        GradeItem item = requireOwnedItem(request.getGradeItemId(), teacherUsername);

        if (request.getComment() == null || request.getComment().isBlank()) {
            // Ручная правка балла должна быть объяснена: студент видит
            // комментарий рядом с оценкой и понимает, откуда она взялась.
            throw AppException.badRequest("Укажите причину: комментарий к ручной оценке обязателен");
        }
        if (request.getScore() > item.getMaxScore()) {
            throw AppException.badRequest("Балл больше веса элемента (" + item.getMaxScore() + ")");
        }

        Long courseId = item.getCourse().getId();
        if (!userCourseRepository.existsByUserIdAndCourseId(request.getUserId(), courseId)) {
            throw AppException.badRequest("Студент не записан на этот курс");
        }

        User student = userRepository.findById(request.getUserId())
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));
        User teacher = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> AppException.unauthorized("Требуется вход в систему"));

        Grade grade = gradeRepository.findByGradeItemIdAndUserId(item.getId(), student.getId())
                .orElseGet(() -> {
                    Grade fresh = new Grade();
                    fresh.setGradeItem(item);
                    fresh.setUser(student);
                    return fresh;
                });

        grade.setScore(request.getScore());
        grade.setManual(true);
        grade.setComment(request.getComment());
        grade.setGradedBy(teacher);
        grade.setUpdatedAt(LocalDateTime.now());
        gradeRepository.save(grade);
    }

    @Override
    @Transactional
    public void clearManualGrade(Long gradeItemId, Long userId, String teacherUsername) {
        GradeItem item = requireOwnedItem(gradeItemId, teacherUsername);

        gradeRepository.findByGradeItemIdAndUserId(gradeItemId, userId)
                .ifPresent(gradeRepository::delete);

        // Ручного балла больше нет — возвращаем клетку под решения студента.
        if (item.getKind() == GradeItemKind.PROBLEM) {
            userRepository.findById(userId)
                    .ifPresent(student -> calculator.recompute(student, item.getStep().getProblem().getId(), List.of(item)));
        }
    }

    // --- Оценки студента ---------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public MyGradesDto myGrades(Long courseId, String username) {
        Course course = courseAccess.requireEnrolled(courseId, username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.unauthorized("Требуется вход в систему"));

        List<GradeItem> items = gradeItemRepository.findAllByCourseIdOrderByOrderIndexAsc(courseId);
        Map<Long, Grade> byItem = gradeRepository.findAllByCourseIdAndUserId(courseId, user.getId()).stream()
                .collect(Collectors.toMap(grade -> grade.getGradeItem().getId(), grade -> grade));

        List<MyGradeDto> grades = items.stream()
                .map(item -> {
                    Grade grade = byItem.get(item.getId());
                    return new MyGradeDto(
                            item.getId(),
                            item.getKind().name(),
                            item.getStep() == null ? null : item.getStep().getId(),
                            lessonIdOf(item),
                            item.getTitle(),
                            item.getMaxScore(),
                            grade == null ? null : grade.getScore(),
                            grade == null ? null : grade.getComment());
                })
                .toList();

        int totalScore = grades.stream().mapToInt(g -> g.score() == null ? 0 : g.score()).sum();
        int totalMaxScore = items.stream().mapToInt(GradeItem::getMaxScore).sum();

        return new MyGradesDto(courseId, course.getTitle(), grades, totalScore, totalMaxScore,
                percent(totalScore, totalMaxScore));
    }

    // --- Внутреннее ---------------------------------------------------------

    /** Понижение веса не должно оставить в журнале балл выше максимума. */
    private void capManualGrades(GradeItem item) {
        gradeRepository.findAllByCourseId(item.getCourse().getId()).stream()
                .filter(grade -> grade.getGradeItem().getId().equals(item.getId()))
                .filter(grade -> grade.getScore() > item.getMaxScore())
                .forEach(grade -> {
                    grade.setScore(item.getMaxScore());
                    gradeRepository.save(grade);
                });
    }

    private void recomputeItemForAllStudents(GradeItem item) {
        Long problemId = item.getStep().getProblem().getId();
        userCourseRepository.findAllByCourseId(item.getCourse().getId())
                .forEach(enrollment -> calculator.recompute(enrollment.getUser(), problemId, List.of(item)));
    }

    private GradebookRowDto toRow(User student, Map<Long, Map<Long, Grade>> byUser, int totalMaxScore) {
        Map<Long, Grade> studentGrades = byUser.getOrDefault(student.getId(), Map.of());

        Map<Long, GradeCellDto> cells = studentGrades.values().stream()
                .collect(Collectors.toMap(
                        grade -> grade.getGradeItem().getId(),
                        grade -> new GradeCellDto(grade.getScore(), grade.isManual(), grade.getComment())));

        int total = studentGrades.values().stream().mapToInt(Grade::getScore).sum();

        return new GradebookRowDto(
                student.getId(),
                student.getUsername(),
                displayName(student),
                cells,
                total,
                percent(total, totalMaxScore));
    }

    private GradeItemDto toItemDto(GradeItem item) {
        return new GradeItemDto(
                item.getId(),
                item.getKind().name(),
                item.getStep() == null ? null : item.getStep().getId(),
                lessonIdOf(item),
                item.getTitle(),
                item.getMaxScore(),
                item.getPolicy().name(),
                item.getOrderIndex(),
                usageHint(item));
    }

    private Long lessonIdOf(GradeItem item) {
        return item.getStep() == null ? null : item.getStep().getLesson().getId();
    }

    /** «Модуль 1 · Урок 2 · шаг 3» — чтобы столбец журнала можно было найти в курсе. */
    private String usageHint(GradeItem item) {
        if (item.getStep() == null) {
            return null;
        }
        Lesson lesson = item.getStep().getLesson();
        return "Модуль " + lesson.getModule().getPositionIndex()
                + " · Урок " + lesson.getOrderIndex()
                + " · шаг " + item.getStep().getOrderIndex();
    }

    private GradeItem requireOwnedItem(Long itemId, String teacherUsername) {
        GradeItem item = gradeItemRepository.findById(itemId)
                .orElseThrow(() -> AppException.notFound("Элемент журнала не найден"));
        courseAccess.requireAuthor(item.getCourse().getId(), teacherUsername);
        return item;
    }

    private double percent(int score, int maxScore) {
        return maxScore == 0 ? 0.0 : Math.round(score * 1000.0 / maxScore) / 10.0;
    }

    private String displayName(User user) {
        return user.getName() == null || user.getName().isBlank() ? user.getUsername() : user.getName();
    }

    private void appendRow(StringBuilder csv, List<String> cells) {
        csv.append(cells.stream().map(this::escapeCsv).collect(Collectors.joining(";")));
        csv.append("\r\n");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
