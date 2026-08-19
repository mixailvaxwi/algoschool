package com.algoschool;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Поднимает полный контекст приложения — ловит ошибки связывания бинов,
 * которые срезовые тесты не видят.
 * <p>
 * Помечен как integration и по умолчанию пропускается: нужны живая MySQL и
 * заданный JWT_SECRET. Запуск: {@code ./mvnw verify -Pintegration} с
 * выставленными DB_URL, DB_PASSWORD и JWT_SECRET (в CI это делает сервис mysql).
 */
@Tag("integration")
@SpringBootTest
class AlgoSchoolApplicationTests {

    @Test
    void contextLoads() {
    }
}
