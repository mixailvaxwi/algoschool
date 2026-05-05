package com.mpanyavin.algoschool.module_course.service;

import com.mpanyavin.algoschool.module_course.dto.CoursePurchaseResponse;

public interface CourseService {
    CoursePurchaseResponse purchaseCourse(Long userId, Long courseId);
}