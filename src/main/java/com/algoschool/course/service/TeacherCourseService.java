package com.algoschool.course.service;

import com.algoschool.exception.AppException;

import com.algoschool.course.dto.TeacherCourseRequest;
import com.algoschool.course.dto.teacher.CourseDto;
import com.algoschool.course.entity.Course;
import com.algoschool.course.repository.CourseRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherCourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseAccessService courseAccess;

    /**
     * Получение всех курсов конкретного преподавателя
     */
    @Transactional(readOnly = true)
    public List<CourseDto> getAllCoursesForTeacher(String username) {
        // Мы используем поиск по username автора, чтобы учитель не видел чужие черновики
        return courseRepository.findAllByAuthorUsername(username).stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Создание нового курса (всегда создается как черновик)
     */
    @Transactional
    public CourseDto createCourse(TeacherCourseRequest request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Преподаватель не найден"));

        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setAccessType(request.accessType());
        course.setAuthor(author);

        // Жестко задаем статус черновика при создании
        course.setPublished(false);

        Course savedCourse = courseRepository.save(course);
        return mapToDto(savedCourse);
    }

    /**
     * Обновление данных курса
     */
    @Transactional
    public CourseDto updateCourse(Long courseId, TeacherCourseRequest request, String username) {
        Course course = findAndVerifyCourse(courseId, username);

        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setAccessType(request.accessType());
        // При редактировании статус публикации обычно не меняем,
        // но если нужно — можно принудительно снимать с публикации

        return mapToDto(courseRepository.save(course));
    }

    /**
     * Переключение статуса Публикация / Черновик
     */
    @Transactional
    public void togglePublishStatus(Long courseId, String username) {
        Course course = findAndVerifyCourse(courseId, username);

        course.setPublished(!course.isPublished());
        courseRepository.save(course);
    }

    /**
     * Удаление курса
     */
    @Transactional
    public void deleteCourse(Long courseId, String username) {
        Course course = findAndVerifyCourse(courseId, username);
        courseRepository.delete(course);
    }

    // --- Вспомогательные методы ---

    private Course findAndVerifyCourse(Long courseId, String username) {
        // Единая проверка авторства живёт в CourseAccessService
        return courseAccess.requireAuthor(courseId, username);
    }

    private CourseDto mapToDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .accessType(course.getAccessType())
                .isPublished(course.isPublished())
                .build();
    }
}