package com.algoschool.course.repository;

import com.algoschool.course.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    /** Id курса, которому принадлежит модуль — без подтягивания ленивых связей. */
    @Query("SELECT m.course.id FROM Module m WHERE m.id = :moduleId")
    Optional<Long> findCourseIdByModuleId(@Param("moduleId") Long moduleId);
}
