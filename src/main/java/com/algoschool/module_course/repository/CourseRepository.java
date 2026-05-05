package com.algoschool.module_course.repository;

import com.algoschool.module_course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // Гостям показываем только те курсы, которые администратор отметил как опубликованные
    List<Course> findAllByIsPublishedTrue();
}