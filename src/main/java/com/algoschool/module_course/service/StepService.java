package com.algoschool.module_course.service;

import com.algoschool.module_assessment.entity.Step;
import com.algoschool.module_course.dto.TeacherStepRequest;

public interface StepService {
    void addStepToLesson(Long lessonId, TeacherStepRequest request);
}