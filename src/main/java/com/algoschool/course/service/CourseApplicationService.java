package com.algoschool.course.service;

import com.algoschool.exception.AppException;

import com.algoschool.course.dto.ApplicationResponse;
import com.algoschool.course.entity.*;
import com.algoschool.course.repository.CourseApplicationRepository;
import com.algoschool.course.repository.CourseRepository;
import com.algoschool.course.repository.UserCourseRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
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

    // Студент подает заявку
    @Transactional
    public void applyToCourse(Long studentId, Long courseId, String message) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> AppException.notFound("Курс не найден"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> AppException.notFound("Студент не найден"));

        if (course.getAccessType() == AccessType.OPEN) {
            throw AppException.badRequest("Этот курс открытый, заявка не требуется.");
        }

        if (applicationRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw AppException.conflict("Вы уже подали заявку на этот курс.");
        }

        if (userCourseRepository.existsByUserIdAndCourseId(studentId, courseId)) {
            throw AppException.conflict("Вы уже зачислены на этот курс.");
        }

        CourseApplication application = CourseApplication.builder()
                .student(student)
                .course(course)
                .motivationMessage(message)
                .status(ApplicationStatus.PENDING)
                .build();
        applicationRepository.save(application);
    }

    // --- НОВЫЙ МЕТОД ДЛЯ РЕФАКТОРИНГА ---
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getUserApplications(Long studentId) {
        return applicationRepository.findAllByStudentId(studentId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // Метод конвертации в DTO
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

    // Обертка для подачи заявки по логину
    @Transactional
    public void applyToCourse(String username, Long courseId, String message) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Студент не найден"));
        applyToCourse(student.getId(), courseId, message);
    }

    // Обертка для получения заявок по логину
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getUserApplications(String username) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Студент не найден"));
        return getUserApplications(student.getId());
    }
}