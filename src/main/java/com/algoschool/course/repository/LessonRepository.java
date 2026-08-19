package com.algoschool.course.repository;

import com.algoschool.course.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /** Id курса, которому принадлежит урок — без подтягивания ленивых связей. */
    @Query("SELECT l.module.course.id FROM Lesson l WHERE l.id = :lessonId")
    Optional<Long> findCourseIdByLessonId(@Param("lessonId") Long lessonId);
}
