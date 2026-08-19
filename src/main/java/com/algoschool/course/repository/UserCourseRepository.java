package com.algoschool.course.repository;

import com.algoschool.course.entity.Course;
import com.algoschool.course.entity.UserCourse;
import com.algoschool.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    boolean existsByUserAndCourse(User user, Course course);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    List<UserCourse> findAllByUserId(Long userId);

    /** Снятие с курса при отзыве одобрения заявки. */
    void deleteByUserIdAndCourseId(Long userId, Long courseId);
}
