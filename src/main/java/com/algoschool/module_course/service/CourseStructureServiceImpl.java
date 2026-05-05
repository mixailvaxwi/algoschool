package com.algoschool.module_course.service;

import com.algoschool.module_course.dto.TeacherLessonRequest;
import com.algoschool.module_course.dto.TeacherModuleRequest;
import com.algoschool.module_course.dto.CourseStructureResponse;
import com.algoschool.module_course.entity.Course;
import com.algoschool.module_course.entity.Lesson;
import com.algoschool.module_course.entity.Module;
import com.algoschool.module_course.repository.CourseRepository;
import com.algoschool.module_course.repository.LessonRepository;
import com.algoschool.module_course.repository.ModuleRepository;
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

    @Override
    @Transactional(readOnly = true)
    public List<CourseStructureResponse> getCourseStructure(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

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
    public Module addModule(Long courseId, TeacherModuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        Module module = new Module();
        module.setTitle(request.title());
        module.setPositionIndex(request.orderIndex());
        module.setCourse(course);

        return moduleRepository.save(module);
    }

    @Override
    @Transactional
    public Lesson addLesson(Long moduleId, TeacherLessonRequest request) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Модуль не найден"));

        Lesson lesson = new Lesson();
        lesson.setTitle(request.title());
        lesson.setOrderIndex(request.orderIndex());
        lesson.setModule(module);

        return lessonRepository.save(lesson);
    }
}