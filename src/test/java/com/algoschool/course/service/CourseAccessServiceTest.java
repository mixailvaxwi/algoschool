package com.algoschool.course.service;

import com.algoschool.course.entity.Course;
import com.algoschool.course.repository.CourseRepository;
import com.algoschool.course.repository.LessonRepository;
import com.algoschool.course.repository.ModuleRepository;
import com.algoschool.course.repository.UserCourseRepository;
import com.algoschool.exception.AppException;
import com.algoschool.step.repository.StepRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Проверки доступа к курсу. Именно здесь закрываются дыры, из-за которых
 * посторонний мог править чужой курс, а незаписанный — читать уроки.
 */
@ExtendWith(MockitoExtension.class)
class CourseAccessServiceTest {

    private static final String AUTHOR = "teacher1";
    private static final String STRANGER = "student1";

    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserCourseRepository userCourseRepository;
    @Mock private ModuleRepository moduleRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private StepRepository stepRepository;

    @InjectMocks private CourseAccessService access;

    private User author;
    private User stranger;

    @BeforeEach
    void setUp() {
        author = User.builder().id(1L).username(AUTHOR).build();
        stranger = User.builder().id(2L).username(STRANGER).build();
    }

    private Course course(boolean published) {
        return Course.builder().id(10L).title("Курс").author(author).isPublished(published).build();
    }

    private void givenCourse(Course course) {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
    }

    // --- requireVisible ---------------------------------------------------

    @Test
    void publishedCourseIsVisibleToAnonymous() {
        givenCourse(course(true));
        assertThat(access.requireVisible(10L, null)).isNotNull();
    }

    /** Чужой черновик отдаём как 404, чтобы не подтверждать сам факт его существования. */
    @Test
    void draftCourseIsHiddenFromStrangersAsNotFound() {
        givenCourse(course(false));
        assertThatThrownBy(() -> access.requireVisible(10L, STRANGER))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void draftCourseIsVisibleToItsAuthor() {
        givenCourse(course(false));
        assertThat(access.requireVisible(10L, AUTHOR)).isNotNull();
    }

    @Test
    void missingCourseIsNotFound() {
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> access.requireVisible(99L, AUTHOR))
                .isInstanceOf(AppException.class);
    }

    // --- requireAuthor ----------------------------------------------------

    @Test
    void authorMayManageOwnCourse() {
        givenCourse(course(true));
        assertThat(access.requireAuthor(10L, AUTHOR)).isNotNull();
    }

    @Test
    void strangerMayNotManageSomeoneElsesCourse() {
        givenCourse(course(true));
        assertThatThrownBy(() -> access.requireAuthor(10L, STRANGER))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- requireEnrolled --------------------------------------------------

    @Test
    void enrolledStudentMayReadCourseContent() {
        givenCourse(course(true));
        when(userRepository.findByUsername(STRANGER)).thenReturn(Optional.of(stranger));
        when(userCourseRepository.existsByUserIdAndCourseId(2L, 10L)).thenReturn(true);

        assertThat(access.requireEnrolled(10L, STRANGER)).isNotNull();
    }

    @Test
    void notEnrolledStudentIsForbidden() {
        givenCourse(course(true));
        when(userRepository.findByUsername(STRANGER)).thenReturn(Optional.of(stranger));
        when(userCourseRepository.existsByUserIdAndCourseId(2L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> access.requireEnrolled(10L, STRANGER))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    /** Преподаватель не записан на собственный курс, но открывать его должен. */
    @Test
    void authorMayReadOwnCourseWithoutEnrolling() {
        givenCourse(course(true));
        assertThat(access.requireEnrolled(10L, AUTHOR)).isNotNull();
    }

    @Test
    void anonymousMayNotReadCourseContent() {
        givenCourse(course(true));
        lenient().when(userRepository.findByUsername(CourseAccessService.ANONYMOUS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> access.requireEnrolled(10L, CourseAccessService.ANONYMOUS))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --- вложенные объекты -------------------------------------------------

    @Test
    void stepInheritsAccessRulesOfItsCourse() {
        when(stepRepository.findCourseIdByStepId(5L)).thenReturn(Optional.of(10L));
        givenCourse(course(true));
        when(userRepository.findByUsername(STRANGER)).thenReturn(Optional.of(stranger));
        when(userCourseRepository.existsByUserIdAndCourseId(2L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> access.requireEnrolledForStep(5L, STRANGER))
                .isInstanceOf(AppException.class);
    }

    @Test
    void moduleOfAnotherTeacherIsNotEditable() {
        when(moduleRepository.findCourseIdByModuleId(7L)).thenReturn(Optional.of(10L));
        givenCourse(course(true));

        assertThatThrownBy(() -> access.requireAuthorOfModule(7L, STRANGER))
                .isInstanceOf(AppException.class);
    }
}
