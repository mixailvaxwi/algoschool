package com.algoschool.step.entity;

import com.algoschool.step.entity.Step;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "step_file_problems")
@Getter
@Setter
public class FileProblem extends Problem {

    @Column(name = "allowed_extensions")
    private String allowedExtensions; // Пример: ".zip,.pdf"

    @Column(name = "max_file_size_mb")
    private Integer maxFileSizeMb;
}