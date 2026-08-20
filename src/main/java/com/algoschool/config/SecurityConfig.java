package com.algoschool.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable) // JWT вместо сессий — CSRF-токен не нужен
            .authorizeHttpRequests(auth -> auth
                    // --- Публичное ---
                    .requestMatchers("/api/auth/**").permitAll()
                    // Витрина: каталог, карточка курса и его программа доступны без входа.
                    // Шаблон {courseId:\d+} намеренно узкий — он НЕ матчит
                    // /api/courses/enrolled и не открывает /lessons/**, поэтому
                    // правила ниже не зависят от порядка объявления.
                    // Черновики отсекаются не здесь, а в StudentCourseService:
                    // маршрут публичный, но неопубликованный курс отдаёт 404.
                    .requestMatchers(HttpMethod.GET, "/api/courses").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/courses/{courseId:\\d+}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/courses/{courseId:\\d+}/structure").permitAll()

                    // --- Кабинет преподавателя ---
                    .requestMatchers("/api/teacher/**").hasRole("TEACHER")

                    // --- Панель администратора ---
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // --- Всё остальное ---
                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint) // 401, а не 403
                    .accessDeniedHandler(accessDeniedHandler)           // 403 в формате ErrorResponse
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS для дев-сервера фронтенда (Vite — 5173, CRA — 3000)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
