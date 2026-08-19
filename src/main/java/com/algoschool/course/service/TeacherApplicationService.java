package com.algoschool.course.service;

import com.algoschool.exception.AppException;

import com.algoschool.course.dto.teacher.ApplicationDto;
import com.algoschool.course.dto.teacher.ApplicationStatusUpdateRequest;
import com.algoschool.course.entity.ApplicationStatus;
import com.algoschool.course.entity.Course;
import com.algoschool.course.entity.CourseApplication;
import com.algoschool.course.entity.UserCourse;
import com.algoschool.course.repository.CourseApplicationRepository;
import com.algoschool.course.repository.CourseRepository;
import com.algoschool.course.repository.UserCourseRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherApplicationService {

    private final CourseApplicationRepository applicationRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserRepository userRepository;
    private final CourseAccessService courseAccess;

    // Получить все заявки для конкретного курса
    @Transactional(readOnly = true)
    public List<ApplicationDto> getCourseApplications(String teacherUsername, Long courseId) {
        Course course = getCourseAndVerifyAuthor(courseId, teacherUsername);

        // В идеале в CourseApplicationRepository нужно добавить метод:
        // List<CourseApplication> findByCourseOrderByCreatedAtDesc(Course course);
        // Пока используем findAll и фильтруем в памяти (для небольших объемов сойдет)
        return applicationRepository.findByCourseOrderByCreatedAtDesc(course).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void changeApplicationStatus(String teacherUsername, Long courseId, Long applicationId, ApplicationStatus newStatus) {
        User teacher = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> AppException.notFound("Преподаватель не найден"));

        CourseApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> AppException.notFound("Заявка не найдена"));

        // Проверка: принадлежит ли курс этому преподавателю
        if (!application.getCourse().getAuthor().getId().equals(teacher.getId()) || !application.getCourse().getId().equals(courseId)) {
            throw AppException.forbidden("Нет прав на изменение этой заявки");
        }

        // Меняем статус
        application.setStatus(newStatus);
        applicationRepository.save(application);

        // САМОЕ ВАЖНОЕ: Если одобрили - зачисляем на курс
        if (newStatus == ApplicationStatus.APPROVED) {
            boolean alreadyEnrolled = userCourseRepository.existsByUserAndCourse(application.getStudent(), application.getCourse());
            if (!alreadyEnrolled) {
                UserCourse enrollment = UserCourse.builder()
                        .user(application.getStudent())
                        .course(application.getCourse())
                        .build();
                userCourseRepository.save(enrollment);
            }
        }
    }

    // Вспомогательный метод для проверки прав учителя
    private Course getCourseAndVerifyAuthor(Long courseId, String teacherUsername) {
        return courseAccess.requireAuthor(courseId, teacherUsername);
    }

    // Вспомогательный метод для маппинга Entity -> DTO
    private ApplicationDto mapToDto(CourseApplication app) {
        String studentName = app.getStudent().getName() != null
                ? app.getStudent().getName()
                : app.getStudent().getUsername();

        return ApplicationDto.builder()
                .id(app.getId())
                .studentId(app.getStudent().getId())
                .studentName(studentName)
                .studentEmail(app.getStudent().getEmail())
                .motivationMessage(app.getMotivationMessage())
                .status(app.getStatus())
                .createdAt(app.getCreatedAt())
                .build();
    }
}