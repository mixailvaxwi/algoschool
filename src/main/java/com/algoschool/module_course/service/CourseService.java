package com.algoschool.module_course.service;

import com.algoschool.module_course.dto.CourseCatalogResponse;
import com.algoschool.module_course.dto.CourseInfoResponse;
import com.algoschool.module_course.dto.TeacherCourseRequest;
import com.algoschool.module_course.dto.CourseStructureResponse;
import com.algoschool.module_course.entity.Course;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface CourseService {

    void enrollInCourse(String username, Long courseId);

    List<CourseCatalogResponse> getCatalog(Long userId);

    Course getCourseById(@PathVariable Long courseId);

    CourseInfoResponse getCourseInfo(Long courseId, String username);

    List<CourseStructureResponse> getCourseStructure(Long courseId);

    List<Course> getAllCoursesForTeacher(String username);

    Course createCourse(TeacherCourseRequest request, String username);

    Course updateCourse(Long courseId, TeacherCourseRequest request);

    void deleteCourse(Long courseId);
}