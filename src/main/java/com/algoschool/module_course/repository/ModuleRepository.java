package com.algoschool.module_course.repository;

import com.algoschool.module_course.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
}
