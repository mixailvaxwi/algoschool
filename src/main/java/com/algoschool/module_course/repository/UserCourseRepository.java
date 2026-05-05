package com.mpanyavin.algoschool.module_course.repository;

import com.mpanyavin.algoschool.module_course.entity.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    // Проверяет, есть ли уже связь между этим пользователем и курсом
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}