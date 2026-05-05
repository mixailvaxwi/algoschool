package com.algoschool.module_course.service;

import com.algoschool.module_course.dto.TeacherLessonRequest;
import com.algoschool.module_course.dto.TeacherModuleRequest;
import com.algoschool.module_course.dto.CourseStructureResponse;
import com.algoschool.module_course.entity.Lesson;
import com.algoschool.module_course.entity.Module;

import java.util.List;

public interface CourseStructureService {
    List<CourseStructureResponse> getCourseStructure(Long courseId);
    Module addModule(Long courseId, TeacherModuleRequest request);
    Lesson addLesson(Long moduleId, TeacherLessonRequest request);
}