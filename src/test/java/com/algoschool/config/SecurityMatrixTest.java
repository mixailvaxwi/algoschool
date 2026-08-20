package com.algoschool.config;

import com.algoschool.admin.controller.AdminUserController;
import com.algoschool.admin.service.AdminUserService;
import com.algoschool.auth.service.JwtService;
import com.algoschool.course.controller.StudentCourseController;
import com.algoschool.course.controller.TeacherCourseController;
import com.algoschool.course.service.StudentCourseService;
import com.algoschool.course.service.TeacherCourseService;
import com.algoschool.exception.GlobalExceptionHandler;
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
@WebMvcTest(controllers = {TeacherCourseController.class, StudentCourseController.class, UserController.class, AdminUserController.class})
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
