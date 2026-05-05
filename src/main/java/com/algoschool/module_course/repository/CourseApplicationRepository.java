package com.algoschool.module_course.repository;

import com.algoschool.module_course.entity.ApplicationStatus;
import com.algoschool.module_course.entity.CourseApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseApplicationRepository extends JpaRepository<CourseApplication, Long> {

    // Для учителя: получить все заявки на конкретный курс со статусом PENDING
    List<CourseApplication> findAllByCourseIdAndStatus(Long courseId, ApplicationStatus status);

    // Для студента: получить все его заявки
    List<CourseApplication> findAllByStudentId(Long studentId);

    // Проверка, подавал ли уже студент заявку на этот курс
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}