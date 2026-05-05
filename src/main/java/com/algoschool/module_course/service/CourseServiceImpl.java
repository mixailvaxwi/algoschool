package com.mpanyavin.algoschool.module_course.service;

import com.mpanyavin.algoschool.module_course.dto.CoursePurchaseResponse;
import com.mpanyavin.algoschool.module_course.entity.Course;
import com.mpanyavin.algoschool.module_course.entity.UserCourse;
import com.mpanyavin.algoschool.module_course.repository.CourseRepository;
import com.mpanyavin.algoschool.module_course.repository.UserCourseRepository;
import com.mpanyavin.algoschool.module_user.entity.User;
import com.mpanyavin.algoschool.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;

    @Override
    @Transactional // Гарантирует целостность финансовой операции
    public CoursePurchaseResponse purchaseCourse(Long userId, Long courseId) {

        // 1. Достаем сущности из БД
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        // 2. Проверяем, опубликован ли курс вообще
        if (!course.getIsPublished()) {
            throw new RuntimeException("Этот курс недоступен для покупки");
        }

        // 3. Проверяем, не купил ли студент его ранее
        if (userCourseRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new RuntimeException("Вы уже приобрели этот курс");
        }

        // 4. Логика оплаты
        int price = course.getPrice();
        if (price > 0) {
            // Если курс платный, проверяем баланс
            if (user.getBalance() < price) {
                // Выброс исключения автоматически откатит транзакцию
                throw new RuntimeException("Недостаточно средств на балансе");
            }

            // Списываем валюту
            user.setBalance(user.getBalance() - price);
            userRepository.save(user); // Hibernate оптимизирует это и сделает UPDATE в конце транзакции
        }

        // 5. Выдаем доступ (создаем связующую запись)
        UserCourse userCourse = UserCourse.builder()
                .user(user)
                .course(course)
                .purchasePrice(price) // Фиксируем, за сколько реально купили (0, если был бесплатным)
                .build();

        userCourseRepository.save(userCourse);

        return new CoursePurchaseResponse(
                true,
                "Курс успешно приобретен!",
                user.getBalance()
        );
    }
}