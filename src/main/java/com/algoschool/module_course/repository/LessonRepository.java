package com.algoschool.module_course.repository;

import com.algoschool.module_course.entity.Lesson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Переопределяем стандартный метод findById, чтобы он жадно грузил шаги
    @EntityGraph(attributePaths = {"steps"})
    Optional<Lesson> findById(Long id);

}