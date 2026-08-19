package com.algoschool.course.repository;

import com.algoschool.course.entity.Course;
import com.algoschool.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByIsPublishedTrue();

    List<Course> findAllByAuthorUsername(String username);
}