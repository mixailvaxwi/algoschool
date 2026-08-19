package com.algoschool.course.service;

import com.algoschool.course.entity.Course;
import com.algoschool.course.repository.CourseRepository;
import com.algoschool.course.repository.LessonRepository;
import com.algoschool.course.repository.ModuleRepository;
import com.algoschool.course.repository.UserCourseRepository;
import com.algoschool.exception.AppException;
import com.algoschool.step.repository.StepRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Единственное место, где решается «можно ли этому пользователю трогать этот курс».
 * <p>
 * Раньше проверка автора была скопирована строковым сравнением в двух сервисах,
 * а в остальных точках входа отсутствовала вовсе — любой авторизованный
 * пользователь мог дописывать модули в чужой курс и читать чужие уроки.
 * <p>
 * Соглашение по кодам ответа: <b>404</b>, когда пользователь не должен знать даже
 * о существовании объекта (чужой черновик), и <b>403</b>, когда объект публично
 * известен, но действие недоступно.
 */
@Service
@RequiredArgsConstructor
public class CourseAccessService {

    /** Имя принципала, которое Spring Security подставляет неаутентифицированным запросам. */
    public static final String ANONYMOUS = "anonymousUser";

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final StepRepository stepRepository;

    // --- Курс ------------------------------------------------------------

    /**
     * Курс виден: он опубликован либо запрашивающий — его автор.
     * Чужой черновик неотличим от несуществующего курса.
     */
    @Transactional(readOnly = true)
    public Course requireVisible(Long courseId, String username) {
        Course course = findCourse(courseId);
        if (course.isPublished() || isAuthor(course, username)) {
            return course;
        }
        throw AppException.notFound("Курс не найден");
    }

    /** Курс, которым владеет username. Иначе — 404, чтобы не раскрывать чужие черновики. */
    @Transactional(readOnly = true)
    public Course requireAuthor(Long courseId, String username) {
        Course course = findCourse(courseId);
        if (!isAuthor(course, username)) {
            throw AppException.notFound("Курс не найден");
        }
        return course;
    }

    /**
     * Доступ к содержимому курса: нужно быть зачисленным. Автор курса проходит
     * всегда — иначе преподаватель не смог бы открыть собственный урок.
     */
    @Transactional(readOnly = true)
    public Course requireEnrolled(Long courseId, String username) {
        Course course = findCourse(courseId);

        if (isAuthor(course, username)) {
            return course;
        }
        if (!course.isPublished()) {
            throw AppException.notFound("Курс не найден");
        }

        User user = requireUser(username);
        if (!userCourseRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
            throw AppException.forbidden("Вы не записаны на этот курс");
        }
        return course;
    }

    // --- Вложенные объекты ------------------------------------------------

    /** Автор курса, которому принадлежит модуль. */
    @Transactional(readOnly = true)
    public Course requireAuthorOfModule(Long moduleId, String username) {
        Long courseId = moduleRepository.findCourseIdByModuleId(moduleId)
                .orElseThrow(() -> AppException.notFound("Модуль не найден"));
        return requireAuthor(courseId, username);
    }

    /** Автор курса, которому принадлежит урок. */
    @Transactional(readOnly = true)
    public Course requireAuthorOfLesson(Long lessonId, String username) {
        Long courseId = lessonRepository.findCourseIdByLessonId(lessonId)
                .orElseThrow(() -> AppException.notFound("Урок не найден"));
        return requireAuthor(courseId, username);
    }

    /** Зачисление на курс, которому принадлежит шаг (отправка решения, отметка о прочтении). */
    @Transactional(readOnly = true)
    public Course requireEnrolledForStep(Long stepId, String username) {
        Long courseId = stepRepository.findCourseIdByStepId(stepId)
                .orElseThrow(() -> AppException.notFound("Шаг не найден"));
        return requireEnrolled(courseId, username);
    }

    // --- Внутреннее -------------------------------------------------------

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> AppException.notFound("Курс не найден"));
    }

    private User requireUser(String username) {
        if (!isNamed(username)) {
            throw AppException.unauthorized("Требуется вход в систему");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.unauthorized("Требуется вход в систему"));
    }

    private boolean isAuthor(Course course, String username) {
        return isNamed(username) && course.getAuthor().getUsername().equals(username);
    }

    private boolean isNamed(String username) {
        return username != null && !username.isBlank() && !username.equals(ANONYMOUS);
    }
}
