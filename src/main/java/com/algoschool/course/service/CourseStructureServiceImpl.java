package com.algoschool.course.service;

import com.algoschool.course.dto.TeacherLessonRequest;
import com.algoschool.course.dto.TeacherModuleRequest;
import com.algoschool.course.dto.CourseStructureResponse;
import com.algoschool.course.entity.Course;
import com.algoschool.course.entity.Lesson;
import com.algoschool.course.entity.Module;
import com.algoschool.course.repository.CourseRepository;
import com.algoschool.course.repository.LessonRepository;
import com.algoschool.course.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseStructureServiceImpl implements CourseStructureService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final CourseAccessService courseAccess;

    @Override
    @Transactional(readOnly = true)
    public List<CourseStructureResponse> getCourseStructure(Long courseId, String username) {
        courseAccess.requireAuthor(courseId, username);
        return getStructureTree(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseStructureResponse> getStructureTree(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> com.algoschool.exception.AppException.notFound("Курс не найден"));

        // Превращаем сущности БД в удобное дерево для фронтенда
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

    @Override
    @Transactional
    public Module addModule(Long courseId, TeacherModuleRequest request, String username) {
        Course course = courseAccess.requireAuthor(courseId, username);

        Module module = new Module();
        module.setTitle(request.title());
        module.setPositionIndex(request.orderIndex());
        module.setCourse(course);

        return moduleRepository.save(module);
    }

    @Override
    @Transactional
    public Lesson addLesson(Long moduleId, TeacherLessonRequest request, String username) {
        // Проверяем право на курс, которому принадлежит модуль: без этого любой
        // авторизованный пользователь мог дописывать уроки в чужой курс.
        courseAccess.requireAuthorOfModule(moduleId, username);

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> com.algoschool.exception.AppException.notFound("Модуль не найден"));

        Lesson lesson = new Lesson();
        lesson.setTitle(request.title());
        lesson.setOrderIndex(request.orderIndex());
        lesson.setModule(module);

        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public Module updateModule(Long moduleId, TeacherModuleRequest request, String username) {
        courseAccess.requireAuthorOfModule(moduleId, username);

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> com.algoschool.exception.AppException.notFound("Модуль не найден"));

        module.setTitle(request.title());
        module.setPositionIndex(request.orderIndex());

        return moduleRepository.save(module);
    }

    @Override
    @Transactional
    public void deleteModule(Long moduleId, String username) {
        courseAccess.requireAuthorOfModule(moduleId, username);

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> com.algoschool.exception.AppException.notFound("Модуль не найден"));

        moduleRepository.delete(module);
    }

    @Override
    @Transactional
    public Lesson updateLesson(Long lessonId, TeacherLessonRequest request, String username) {
        courseAccess.requireAuthorOfLesson(lessonId, username);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> com.algoschool.exception.AppException.notFound("Урок не найден"));

        lesson.setTitle(request.title());
        lesson.setOrderIndex(request.orderIndex());

        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId, String username) {
        courseAccess.requireAuthorOfLesson(lessonId, username);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> com.algoschool.exception.AppException.notFound("Урок не найден"));

        lessonRepository.delete(lesson);
    }
}
