package com.algoschool.grade.dto;

/**
 * Клетка журнала.
 *
 * @param manual балл поставлен руками — пересчёт по решениям его не трогает.
 *               Поле намеренно без приставки «is»: Lombok и Jackson по-разному
 *               обходятся с is-геттерами, и такое поле уже один раз уезжало
 *               в JSON под другим именем.
 */
public record GradeCellDto(
        Integer score,
        boolean manual,
        String comment
) {}
