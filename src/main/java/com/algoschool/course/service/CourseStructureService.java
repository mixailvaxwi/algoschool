package com.algoschool.course.service;

import com.algoschool.course.dto.TeacherLessonRequest;
import com.algoschool.course.dto.TeacherModuleRequest;
import com.algoschool.course.dto.CourseStructureResponse;
import com.algoschool.course.entity.Lesson;
import com.algoschool.course.entity.Module;

import java.util.List;

/**
 * Редактирование структуры курса. Каждый метод принимает username, потому что
 * право на изменение проверяется по автору курса, а не по факту авторизации.
 */
public interface CourseStructureService {
    /** Дерево модулей и уроков без проверки прав — вызывающий проверяет доступ сам. */
    List<CourseStructureResponse> getStructureTree(Long courseId);

    /** То же дерево, но только для автора курса. */
    List<CourseStructureResponse> getCourseStructure(Long courseId, String username);
    Module addModule(Long courseId, TeacherModuleRequest request, String username);
    Lesson addLesson(Long moduleId, TeacherLessonRequest request, String username);

    Module updateModule(Long moduleId, TeacherModuleRequest request, String username);
    void deleteModule(Long moduleId, String username);

    Lesson updateLesson(Long lessonId, TeacherLessonRequest request, String username);
    void deleteLesson(Long lessonId, String username);
}
