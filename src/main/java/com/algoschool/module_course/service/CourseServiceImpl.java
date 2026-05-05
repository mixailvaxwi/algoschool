package com.algoschool.module_course.service;

import com.algoschool.module_course.dto.CourseCatalogResponse;
import com.algoschool.module_course.dto.CourseInfoResponse;
import com.algoschool.module_course.dto.TeacherCourseRequest;
import com.algoschool.module_course.dto.CourseStructureResponse;
import com.algoschool.module_course.entity.AccessType;
import com.algoschool.module_course.entity.Course;
import com.algoschool.module_course.entity.UserCourse;
import com.algoschool.module_course.repository.CourseRepository;
import com.algoschool.module_course.repository.UserCourseRepository;
import com.algoschool.module_user.entity.User;
import com.algoschool.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;

    @Override
    @Transactional
    public void enrollInCourse(String username, Long courseId) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        // Проверяем, открыт ли курс
        if (course.getAccessType() != AccessType.OPEN) {
            throw new RuntimeException("На этот курс нельзя записаться свободно");
        }

        boolean alreadyEnrolled = userCourseRepository.existsByUserIdAndCourseId(student.getId(), course.getId());
        if (!alreadyEnrolled) {
            UserCourse enrollment = new UserCourse();
            enrollment.setUser(student);
            enrollment.setCourse(course);
            enrollment.setEnrolledAt(LocalDateTime.now());
            userCourseRepository.save(enrollment);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseCatalogResponse> getCatalog(Long userId) {
        // Отдаем только опубликованные курсы
        List<Course> availableCourses = courseRepository.findAllByIsPublishedTrue();

        if (userId == null) {
            return availableCourses.stream()
                    .map(c -> new CourseCatalogResponse(c.getId(), c.getTitle(), c.getDescription(), c.getAccessType(), false))
                    .toList();
        }

        Set<Long> enrolledCourseIds = userCourseRepository.findAllByUserId(userId)
                .stream()
                .map(uc -> uc.getCourse().getId())
                .collect(Collectors.toSet());

        return availableCourses.stream()
                .map(c -> new CourseCatalogResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getDescription(),
                        c.getAccessType(), // Вместо c.getPrice()
                        enrolledCourseIds.contains(c.getId())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Course getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        return course;
    }

    public CourseInfoResponse getCourseInfo(Long courseId, String username) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        boolean isEnrolled = false;

        // Если юзер авторизован (не аноним), проверяем его запись на курс
        if (username != null && !username.equals("anonymousUser")) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Проверяем, есть ли связка этого юзера и курса в базе
            isEnrolled = userCourseRepository.existsByUserIdAndCourseId(user.getId(), course.getId());
        }

        return new CourseInfoResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getAccessType().name(),
                isEnrolled,
                "NONE" // Пока хардкодим, если функционал заявок еще не готов
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseStructureResponse> getCourseStructure(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        return course.getModules().stream()
                .map(module -> new CourseStructureResponse(
                        module.getId(),
                        module.getTitle(),
                        module.getPositionIndex(),
                        module.getLessons().stream()
                                .map(lesson -> new CourseStructureResponse.LessonDto(
                                        lesson.getId(),
                                        lesson.getTitle(),
                                        lesson.getOrderIndex()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    // ... предыдущие методы (enrollInCourse, getCatalog, getCourseStructure) остаются без изменений ...

    @Override
    @Transactional(readOnly = true)
    public List<Course> getAllCoursesForTeacher(String username) { // ИЗМЕНЕНО
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));
        return courseRepository.findAll();

        // Примечание: позже здесь стоит добавить фильтрацию (findAllByAuthorId),
        // чтобы преподаватель видел только свои курсы, а не чужие.
    }

    @Override
    @Transactional
    public Course createCourse(TeacherCourseRequest request, String username) { // ИЗМЕНЕНО
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));
        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setAccessType(AccessType.valueOf(request.accessType().toString().toUpperCase()));
        course.setPublished(request.isPublished());
        course.setAuthor(author);
        System.out.println(course.getAuthor());
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course updateCourse(Long courseId, TeacherCourseRequest request) { // ИЗМЕНЕНО
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс с ID " + courseId + " не найден"));
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setAccessType(request.accessType());
        course.setPublished(request.isPublished());
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Курс с ID " + courseId + " не найден");
        }
        courseRepository.deleteById(courseId);
    }
}