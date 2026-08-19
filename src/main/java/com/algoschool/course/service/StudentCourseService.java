package com.algoschool.course.service;

import com.algoschool.course.dto.CourseInfoResponse;
import com.algoschool.course.dto.CourseStructureResponse;
import com.algoschool.course.dto.student.CourseCatalogDto;
import com.algoschool.course.dto.student.EnrollmentRequest;
import com.algoschool.course.dto.student.EnrollmentResponse;
import com.algoschool.course.entity.*;
import com.algoschool.course.repository.CourseApplicationRepository;
import com.algoschool.course.repository.CourseRepository;
import com.algoschool.course.repository.UserCourseRepository;
import com.algoschool.exception.AppException;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentCourseService {

    /** Имя принципала, которое Spring Security подставляет неаутентифицированным запросам. */
    private static final String ANONYMOUS = CourseAccessService.ANONYMOUS;

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;
    private final CourseApplicationRepository applicationRepository;
    private final CourseAccessService courseAccess;

    @Transactional(readOnly = true)
    public List<CourseCatalogDto> getCatalog(String username) {
        List<Course> availableCourses = courseRepository.findAllByIsPublishedTrue();

        Set<Long> enrolledCourseIds = Set.of();
        // Словарь для хранения статусов заявок: courseId -> статус
        Map<Long, String> appStatuses = new java.util.HashMap<>();

        if (username != null && !username.equals(ANONYMOUS)) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                // Ищем, куда уже записан
                enrolledCourseIds = userCourseRepository.findAllByUserId(user.getId()).stream()
                        .map(uc -> uc.getCourse().getId())
                        .collect(Collectors.toSet());

                // Ищем поданные заявки
                applicationRepository.findAllByStudentId(user.getId()).forEach(app -> {
                    appStatuses.put(app.getCourse().getId(), app.getStatus().name());
                });
            }
        }

        final Set<Long> finalEnrolledIds = enrolledCourseIds;
        return availableCourses.stream()
                .map(c -> mapToCatalogDto(c, finalEnrolledIds.contains(c.getId()), appStatuses.getOrDefault(c.getId(), "NONE")))
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseInfoResponse getCourseInfo(Long courseId, String username) {
        Course course = courseAccess.requireVisible(courseId, username);

        boolean isEnrolled = false;
        String appStatus = "NONE";

        if (username != null && !username.equals(ANONYMOUS)) {
            User user = userRepository.findByUsername(username).orElseThrow();
            isEnrolled = userCourseRepository.existsByUserIdAndCourseId(user.getId(), course.getId());

            if (!isEnrolled) {
                appStatus = applicationRepository.findAllByStudentId(user.getId()).stream()
                        .filter(a -> a.getCourse().getId().equals(courseId))
                        .map(a -> a.getStatus().name())
                        .findFirst()
                        .orElse("NONE");
            }
        }

        return new CourseInfoResponse(
                course.getId(), course.getTitle(), course.getDescription(),
                course.getAccessType().name(), isEnrolled, appStatus
        );
    }

    @Transactional(readOnly = true)
    public List<CourseStructureResponse> getCourseStructure(Long courseId, String username) {
        Course course = courseAccess.requireVisible(courseId, username);

        return course.getModules().stream()
                .map(module -> new CourseStructureResponse(
                        module.getId(), module.getTitle(), module.getPositionIndex(),
                        module.getLessons().stream()
                                .map(l -> new CourseStructureResponse.LessonDto(l.getId(), l.getTitle(), l.getOrderIndex()))
                                .toList()
                )).toList();
    }

    // --- МЕТОДЫ ЗАПИСИ (С ПЕРЕГРУЗКОЙ) ---

    // 1. Метод без EnrollmentRequest (как ты просил)
    @Transactional
    public EnrollmentResponse enrollInCourse(String username, Long courseId) {
        return enrollInCourse(username, courseId, null);
    }

    // 2. Основной метод с EnrollmentRequest (универсальный)
    @Transactional
    public EnrollmentResponse enrollInCourse(String username, Long courseId, EnrollmentRequest request) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> AppException.notFound("Курс не найден"));

        if (!course.isPublished()) throw AppException.conflict("Курс еще не опубликован");

        if (userCourseRepository.existsByUserAndCourse(student, course)) {
            throw AppException.conflict("Вы уже записаны на этот курс");
        }

        // Если курс ЗАКРЫТЫЙ - создаем заявку
        if (course.getAccessType() == AccessType.CLOSED) {
            if (applicationRepository.existsByStudentAndCourseAndStatus(student, course, ApplicationStatus.PENDING)) {
                throw AppException.conflict("Заявка уже на рассмотрении");
            }

            CourseApplication application = CourseApplication.builder()
                    .student(student).course(course).status(ApplicationStatus.PENDING)
                    .motivationMessage(request != null ? request.getMotivationMessage() : null)
                    .build();
            applicationRepository.save(application);

            return EnrollmentResponse.builder()
                    .courseId(course.getId()).status(ApplicationStatus.PENDING)
                    .message("Заявка отправлена преподавателю").build();
        }

        // Если курс ОТКРЫТЫЙ - записываем сразу
        UserCourse enrollment = UserCourse.builder().user(student).course(course).build();
        userCourseRepository.save(enrollment);

        return EnrollmentResponse.builder()
                .courseId(course.getId()).status(ApplicationStatus.APPROVED)
                .message("Вы успешно записаны на курс!").build();
    }

    // Вспомогательный маппер
    private CourseCatalogDto mapToCatalogDto(Course course, boolean isEnrolled, String appStatus) {
        return CourseCatalogDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .accessType(course.getAccessType())
                .authorName(course.getAuthor().getName() != null ? course.getAuthor().getName() : course.getAuthor().getUsername())
                .isEnrolled(isEnrolled)
                .applicationStatus(appStatus) // <--- передаем статус
                .build();
    }

    @Transactional(readOnly = true)
    public List<CourseCatalogDto> getEnrolledCourses(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));

        // Ищем все записи в user_courses для этого пользователя
        return userCourseRepository.findAllByUserId(user.getId()).stream()
                .map(uc -> {
                    Course c = uc.getCourse();
                    // Для этих курсов isEnrolled всегда true
                    return mapToCatalogDto(c, true, "NONE");
                })
                .toList();
    }
}