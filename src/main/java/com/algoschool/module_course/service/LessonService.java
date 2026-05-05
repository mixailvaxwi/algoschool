package com.algoschool.module_course.service;

import com.algoschool.module_course.dto.LessonPlayerResponse;
import com.algoschool.security.UserDetailsImpl;

public interface LessonService {
    LessonPlayerResponse getFullLesson(Long lessonId, UserDetailsImpl currentUser);

}