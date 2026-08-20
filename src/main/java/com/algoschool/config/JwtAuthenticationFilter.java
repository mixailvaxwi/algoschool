package com.algoschool.config;

import com.algoschool.auth.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SecurityErrorWriter errorWriter;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Заголовка нет — пропускаем дальше. Решение о доступе примет цепочка
        // авторизации: для публичных путей это норма, для остальных сработает
        // RestAuthenticationEntryPoint.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Перечитывается из БД на каждый запрос, поэтому блокировка
                // администратором обрывает уже выданный токен сразу, а не ждёт
                // его истечения — иначе заблокированный аккаунт продолжал бы
                // работать до конца срока действия JWT.
                if (!userDetails.isAccountNonLocked()) {
                    SecurityContextHolder.clearContext();
                    errorWriter.write(response, HttpStatus.UNAUTHORIZED, "Аккаунт заблокирован");
                    return;
                }

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            // Битый, просроченный или подписанный чужим ключом токен. Фильтры
            // работают вне DispatcherServlet, поэтому исключение отсюда не поймает
            // @RestControllerAdvice — без этого блока клиент получал бы HTML-страницу
            // ошибки контейнера и не мог понять, что пора перелогиниться.
            SecurityContextHolder.clearContext();
            errorWriter.write(response, HttpStatus.UNAUTHORIZED, "Токен недействителен или истёк");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
