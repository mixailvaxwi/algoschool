package com.mpanyavin.algoschool.module_course.repository;

import com.mpanyavin.algoschool.module_course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
