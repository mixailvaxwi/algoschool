package com.algoschool.security;

import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Единственная реализация {@link UserDetailsService}.
 * <p>
 * Раньше их было две: этот бин и лямбда в ApplicationConfig. Какая из них
 * попадала в фильтр, решало совпадение имени параметра конструктора с именем
 * бина — переименование параметра молча меняло принципала. Лямбда удалена.
 * <p>
 * Сущность {@code User} сама реализует {@code UserDetails}, поэтому
 * отдельный адаптер (UserDetailsImpl) не нужен.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }
}
