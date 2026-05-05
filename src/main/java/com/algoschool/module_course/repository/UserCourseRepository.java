package com.algoschool.module_course.repository;

import com.algoschool.module_course.entity.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    // Проверяет, есть ли уже связь между этим пользователем и курсом
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // Получить все записи о покупках конкретного пользователя
    List<UserCourse> findAllByUserId(Long userId);


}