package com.algoschool.module_course.service;

import com.algoschool.module_course.dto.ApplicationResponse;
import com.algoschool.module_course.entity.*;
import com.algoschool.module_course.repository.CourseApplicationRepository;
import com.algoschool.module_course.repository.CourseRepository;
import com.algoschool.module_course.repository.UserCourseRepository;
import com.algoschool.module_user.entity.User;
import com.algoschool.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseApplicationService {

    private final CourseApplicationRepository applicationRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;

    // 1. Студент подает заявку
    @Transactional
    public void applyToCourse(Long studentId, Long courseId, String message) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        User student = userRepository.findById(studentId).orElseThrow();

        if (course.getAccessType() == AccessType.OPEN) {
            throw new RuntimeException("Этот курс открытый, заявка не требуется. Записывайтесь напрямую!");
        }

        if (applicationRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Вы уже подали заявку на этот курс.");
        }

        if (userCourseRepository.existsByUserIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Вы уже зачислены на этот курс.");
        }

        CourseApplication application = CourseApplication.builder()
                .student(student)
                .course(course)
                .motivationMessage(message)
                .status(ApplicationStatus.PENDING)
                .build();

        applicationRepository.save(application);
    }

    // 2. Учитель: Одобрить или отклонить заявку
    @Transactional
    public void reviewApplication(Long teacherId, Long appId, boolean isApproved) {
        CourseApplication application = applicationRepository.findById(appId).orElseThrow();
        Course course = application.getCourse();

        // Проверка безопасности: Только автор курса может одобрять заявки
        if (!course.getAuthor().getId().equals(teacherId)) {
            throw new RuntimeException("У вас нет прав на управление этим курсом");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Эта заявка уже обработана");
        }

        if (isApproved) {
            application.setStatus(ApplicationStatus.APPROVED);
            // Автоматически зачисляем студента на курс!
            UserCourse enrollment = UserCourse.builder()
                    .user(application.getStudent())
                    .course(course)
                    .build();
            userCourseRepository.save(enrollment);
        } else {
            application.setStatus(ApplicationStatus.REJECTED);
        }

        applicationRepository.save(application);
    }

    // 3. Утилита для конвертации в DTO
    public ApplicationResponse mapToDto(CourseApplication app) {
        return new ApplicationResponse(
                app.getId(),
                app.getCourse().getId(),
                app.getCourse().getTitle(),
                app.getStudent().getId(),
                app.getStudent().getUsername(),
                app.getMotivationMessage(),
                app.getStatus(),
                app.getCreatedAt()
        );
    }
}