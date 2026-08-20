-- Множественный выбор для ChoiceProblem.
--
-- Раньше правильный ответ хранился как один `correct_option_index`, поэтому
-- `is_multiple_choice` сохранялся, но проверяющий модуль всё равно сравнивал
-- один индекс, а интерфейс рисовал радиокнопки (см. README, «Известные
-- ограничения»). Теперь правильный ответ — это множество индексов: для
-- одиночного выбора оно всегда состоит из одного элемента.

CREATE TABLE `choice_problem_correct_options` (
    `problem_id`   BIGINT(20) NOT NULL,
    `option_index` INT(11)    NOT NULL,
    PRIMARY KEY (`problem_id`, `option_index`),
    CONSTRAINT `fk_choice_correct_options_problem`
        FOREIGN KEY (`problem_id`) REFERENCES `choice_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `choice_problem_correct_options` (`problem_id`, `option_index`)
SELECT `id`, `correct_option_index` FROM `choice_problems`;

ALTER TABLE `choice_problems` DROP COLUMN `correct_option_index`;
