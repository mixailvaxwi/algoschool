package com.algoschool.config;

import com.algoschool.admin.controller.AdminUserController;
import com.algoschool.admin.service.AdminUserService;
import com.algoschool.auth.service.JwtService;
import com.algoschool.course.controller.StudentCourseController;
import com.algoschool.course.controller.TeacherCourseController;
import com.algoschool.grade.controller.StudentGradeController;
import com.algoschool.submission.controller.TeacherReviewController;
import com.algoschool.submission.service.ReviewService;
import com.algoschool.grade.controller.TeacherGradebookController;
import com.algoschool.grade.service.GradeService;
import com.algoschool.problem.controller.TeacherProblemController;
import com.algoschool.problem.service.ProblemService;
import com.algoschool.course.service.StudentCourseService;
import com.algoschool.course.service.TeacherCourseService;
import com.algoschool.exception.GlobalExceptionHandler;
import com.algoschool.grade.dto.GradebookDto;
import com.algoschool.grade.dto.MyGradesDto;
import com.algoschool.user.controller.UserController;
import com.algoschool.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Матрица доступа на уровне маршрутов.
 * <p>
 * Это регрессионный тест на дыру, из-за которой /api/teacher/** не совпадал
 * ни с одним правилом и доставался любому авторизованному пользователю,
 * а неаутентифицированный запрос получал 403 вместо 401.
 */
@WebMvcTest(controllers = {TeacherCourseController.class, StudentCourseController.class, UserController.class,
        AdminUserController.class, TeacherProblemController.class,
        TeacherGradebookController.class, StudentGradeController.class, TeacherReviewController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityErrorWriter.class,
        RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class, GlobalExceptionHandler.class})
class SecurityMatrixTest {

    @Autowired private MockMvc mvc;

    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private AuthenticationProvider authenticationProvider;
    @MockitoBean private TeacherCourseService teacherCourseService;
    @MockitoBean private StudentCourseService studentCourseService;
    @MockitoBean private UserService userService;
    @MockitoBean private AdminUserService adminUserService;
    @MockitoBean private ProblemService problemService;
    @MockitoBean private GradeService gradeService;
    @MockitoBean private ReviewService reviewService;

    // --- Публичная витрина -------------------------------------------------

    @Test
    @WithAnonymousUser
    void catalogIsPublic() throws Exception {
        when(studentCourseService.getCatalog(any())).thenReturn(List.of());
        mvc.perform(get("/api/courses")).andExpect(status().isOk());
    }

    // --- Кабинет преподавателя ---------------------------------------------

    /** Главное: студенческий токен не должен доставать до преподавательских маршрутов. */
    @Test
    @WithMockUser(username = "student1", roles = "STUDENT")
    void studentIsForbiddenFromTeacherArea() throws Exception {
        mvc.perform(get("/api/teacher/courses")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void teacherReachesTeacherArea() throws Exception {
        when(teacherCourseService.getAllCoursesForTeacher(anyString())).thenReturn(List.of());
        mvc.perform(get("/api/teacher/courses")).andExpect(status().isOk());
    }

    /** Без аутентификации — 401, а не 403: фронтенд должен отличать «войдите» от «нет прав». */
    @Test
    @WithAnonymousUser
    void anonymousGetsUnauthorizedOnTeacherArea() throws Exception {
        mvc.perform(get("/api/teacher/courses")).andExpect(status().isUnauthorized());
    }

    // --- Панель администратора -----------------------------------------------

    @Test
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void teacherIsForbiddenFromAdminArea() throws Exception {
        mvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void adminReachesAdminArea() throws Exception {
        when(adminUserService.searchUsers(any())).thenReturn(List.of());
        mvc.perform(get("/api/admin/users")).andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void anonymousGetsUnauthorizedOnAdminArea() throws Exception {
        mvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
    }

    // --- Банк задач ---------------------------------------------------------

    /** Банк живёт внутри /api/teacher/**, поэтому студенческий токен туда не достаёт. */
    @Test
    @WithMockUser(username = "student1", roles = "STUDENT")
    void studentIsForbiddenFromProblemBank() throws Exception {
        mvc.perform(get("/api/teacher/problems")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void teacherReachesProblemBank() throws Exception {
        when(problemService.search(any(), anyString())).thenReturn(List.of());
        mvc.perform(get("/api/teacher/problems")).andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void anonymousGetsUnauthorizedOnProblemBank() throws Exception {
        mvc.perform(get("/api/teacher/problems")).andExpect(status().isUnauthorized());
    }

    // --- Журнал оценок --------------------------------------------------------

    /** Журнал курса — преподавательский маршрут: студенту туда нельзя. */
    @Test
    @WithMockUser(username = "student1", roles = "STUDENT")
    void studentIsForbiddenFromGradebook() throws Exception {
        mvc.perform(get("/api/teacher/courses/1/gradebook")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void teacherReachesGradebook() throws Exception {
        when(gradeService.gradebook(any(), anyString()))
                .thenReturn(new GradebookDto(1L, "Курс", List.of(), List.of(), 0));
        mvc.perform(get("/api/teacher/courses/1/gradebook")).andExpect(status().isOk());
    }

    /** Свои оценки студент видит, но только после входа. */
    @Test
    @WithMockUser(username = "student1", roles = "STUDENT")
    void studentSeesOwnGrades() throws Exception {
        when(gradeService.myGrades(any(), anyString()))
                .thenReturn(new MyGradesDto(1L, "Курс", List.of(), 0, 0, 0.0));
        mvc.perform(get("/api/courses/1/my-grades")).andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void anonymousGetsUnauthorizedOnOwnGrades() throws Exception {
        mvc.perform(get("/api/courses/1/my-grades")).andExpect(status().isUnauthorized());
    }

    // --- Очередь ручной проверки ---------------------------------------------

    /**
     * Развёрнутый ответ видит только проверяющий (§7): студенческий токен до
     * очереди не достаёт.
     */
    @Test
    @WithMockUser(username = "student1", roles = "STUDENT")
    void studentIsForbiddenFromReviewQueue() throws Exception {
        mvc.perform(get("/api/teacher/review-queue").param("courseId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void teacherReachesReviewQueue() throws Exception {
        when(reviewService.queue(any(), anyString())).thenReturn(List.of());
        mvc.perform(get("/api/teacher/review-queue").param("courseId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void anonymousGetsUnauthorizedOnReviewQueue() throws Exception {
        mvc.perform(get("/api/teacher/review-queue").param("courseId", "1"))
                .andExpect(status().isUnauthorized());
    }

    // --- Личные данные ------------------------------------------------------

    @Test
    @WithAnonymousUser
    void anonymousGetsUnauthorizedOnProfile() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    /**
     * /api/courses/enrolled закрыт независимо от порядка правил: узкий шаблон
     * {courseId:\d+} не матчит слово enrolled.
     */
    @Test
    @WithAnonymousUser
    void enrolledCoursesAreNotPublic() throws Exception {
        mvc.perform(get("/api/courses/enrolled")).andExpect(status().isUnauthorized());
    }
}
