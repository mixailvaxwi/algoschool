package com.algoschool.course.repository;

import com.algoschool.course.entity.Course;
import com.algoschool.course.entity.UserCourse;
import com.algoschool.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- Убедитесь, что этот импорт есть

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    boolean existsByUserAndCourse(User user, Course course);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // --- ИСПРАВЛЕНИЕ ОШИБКИ: ДОБАВЛЯЕМ НОВЫЙ МЕТОД ---
    // Возвращает список всех записей (UserCourse) для конкретного студента
    List<UserCourse> findAllByUserId(Long userId);
}