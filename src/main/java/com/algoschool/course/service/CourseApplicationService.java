package com.algoschool.course.service;

import com.algoschool.course.dto.ApplicationResponse;
import com.algoschool.course.entity.CourseApplication;
import com.algoschool.course.repository.CourseApplicationRepository;
import com.algoschool.exception.AppException;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Просмотр студентом собственных заявок.
 * <p>
 * Подача заявки жила здесь во втором, расходящемся варианте: этот запрещал
 * повторную подачу после отказа и отвергал открытые курсы, а
 * {@link StudentCourseService#enrollInCourse} — нет. Теперь единственная точка
 * входа на курс — {@code POST /api/courses/{id}/enroll}, а здесь остаётся
 * только чтение.
 */
@Service
@RequiredArgsConstructor
public class CourseApplicationService {

    private final CourseApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getUserApplications(String username) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Студент не найден"));

        return applicationRepository.findAllByStudentId(student.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private ApplicationResponse mapToDto(CourseApplication app) {
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
