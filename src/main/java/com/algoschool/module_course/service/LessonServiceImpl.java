package com.algoschool.module_course.service;

import com.algoschool.exception.AppException;
import com.algoschool.module_assessment.entity.Step;
import com.algoschool.module_assessment.entity.UserStepProgress;
import com.algoschool.module_assessment.repository.StepRepository;
import com.algoschool.module_assessment.repository.UserStepProgressRepository;
import com.algoschool.module_course.dto.LessonPlayerResponse;
import com.algoschool.module_course.entity.Lesson;
import com.algoschool.module_course.repository.LessonRepository;
import com.algoschool.module_course.repository.UserCourseRepository;
import com.algoschool.module_user.entity.User;
import com.algoschool.security.UserDetailsImpl;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserStepProgressRepository userStepProgressRepository;
    private final StepRepository stepRepository;

    @Override
    @Transactional(readOnly = true)
    public LessonPlayerResponse getFullLesson(Long lessonId, UserDetailsImpl currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Урок с ID " + lessonId + " не найден"));

        if (currentUser == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Вы должны быть авторизованы для просмотра этого урока");
        }

        Long courseId = lesson.getModule().getCourse().getId();
        boolean hasAccess = userCourseRepository.existsByUserIdAndCourseId(currentUser.getId(), courseId);

        if (!hasAccess) {
            throw new AppException(HttpStatus.FORBIDDEN, "У вас нет доступа к этому уроку. Пожалуйста, приобретите курс.");
        }

        // Получаем все завершенные шаги для пользователя и собираем их ID в Set
        Set<Step> completedSteps = userStepProgressRepository.findAllByUserIdAndIsCompletedTrue(currentUser.getId())
                .stream()
                .map(UserStepProgress::getStep)
                .collect(Collectors.toSet());

        return new LessonPlayerResponse(lesson, completedSteps);
    }
}

/*
при попытке запуска выдало:
java: invalid method reference
cannot find symbol
symbol:   method getStepId()
location: class com.algoschool.module_assessment.entity.UserStepProgress

файл  выглядет так:
        package com.algoschool.module_assessment.entity;

import com.algoschool.module_user.entity.User;
import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDateTime;

@Entity
@Table(name = "user_step_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStepProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;

    @Column(name = "is_passed", nullable = false)
    private boolean isPassed;

    // Храним ответ юзера. Для текста - строка, для тестов - ID варианта, для файлов - URL на S3
    @Column(columnDefinition = "TEXT")
    private String submittedPayload;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();
}

 */