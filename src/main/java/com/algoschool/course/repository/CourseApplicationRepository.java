package com.algoschool.course.repository;

import com.algoschool.course.entity.ApplicationStatus;
import com.algoschool.course.entity.Course;
import com.algoschool.course.entity.CourseApplication;
import com.algoschool.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseApplicationRepository extends JpaRepository<CourseApplication, Long> {

    boolean existsByStudentAndCourseAndStatus(User student, Course course, ApplicationStatus status);

    List<CourseApplication> findByCourseOrderByCreatedAtDesc(Course course);

    // --- ИСПРАВЛЕНИЕ ОШИБКИ: ДОБАВЛЯЕМ НОВЫЙ МЕТОД ---
    // Возвращает список всех заявок, поданных конкретным студентом
    List<CourseApplication> findAllByStudentId(Long studentId);
}