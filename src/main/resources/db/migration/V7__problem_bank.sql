-- Банк задач: задача перестаёт быть шагом урока.
--
-- Было: Problem extends Step, то есть строка задачи физически принадлежала
-- одному уроку (step_problems.id -> steps.id). Поставить одну задачу в два
-- урока можно было только копированием, а копия расходилась с оригиналом при
-- правке условия, и счётчики attempted_students_count / success_students_count
-- размазывались по копиям — сложность задачи переставала быть измеримой.
--
-- Стало:
--   problems       — задача как единица содержания и оценивания (банк);
--   problem_steps  — размещение задачи в уроке (подтип steps).
--
-- Данные не копируются: step_problems переименовывается в problems, а его
-- прежние идентификаторы (они же идентификаторы шагов) остаются на месте.
-- Поэтому размещение для каждой существующей задачи создаётся строкой
-- (id, id) — шаг с таким id и есть тот урок, где задача стояла до сих пор.
--
-- Порядок как в V2: сначала приводим данные в согласованный вид, потом вешаем
-- ограничения.

-- --------------------------------------------------------------------
-- 1. step_problems -> problems, собственная последовательность id
-- --------------------------------------------------------------------

-- InnoDB сам переписывает внешние ключи детей (choice_problems, code_problems,
-- text_problems, submissions и прочие) на новое имя таблицы.
RENAME TABLE `step_problems` TO `problems`;

-- Внешний ключ на steps — это и есть та связь «задача = шаг», которую мы
-- разрываем. Имя ключа сгенерировал Hibernate (в V1 это
-- FK5pgugsm9nnvitihqie13ff54m), но полагаться на конкретный хеш не хочется:
-- находим ключ по тому, куда он ведёт.
SET @fk_problem_step := (
    SELECT `constraint_name`
      FROM `information_schema`.`referential_constraints`
     WHERE `constraint_schema` = DATABASE()
       AND `table_name` = 'problems'
       AND `referenced_table_name` = 'steps'
     LIMIT 1
);
SET @drop_fk := IF(@fk_problem_step IS NULL,
                   'SELECT 1',
                   CONCAT('ALTER TABLE `problems` DROP FOREIGN KEY `', @fk_problem_step, '`'));
PREPARE drop_fk_stmt FROM @drop_fk;
EXECUTE drop_fk_stmt;
DEALLOCATE PREPARE drop_fk_stmt;

-- Раньше id выдавала таблица steps. Теперь у банка своя нумерация.
ALTER TABLE `problems`
    MODIFY COLUMN `id` BIGINT(20) NOT NULL AUTO_INCREMENT;

-- --------------------------------------------------------------------
-- 2. Поля, которых у задачи не было, пока она была шагом
-- --------------------------------------------------------------------

ALTER TABLE `problems`
    ADD COLUMN `title`      VARCHAR(200) NULL,
    ADD COLUMN `author_id`  BIGINT(20)   NULL,
    ADD COLUMN `difficulty` VARCHAR(16)  NULL,
    ADD COLUMN `visibility` VARCHAR(16)  NOT NULL DEFAULT 'PRIVATE',
    ADD COLUMN `max_score`  INT(11)      NOT NULL DEFAULT 1,
    ADD COLUMN `created_at` DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN `updated_at` DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

-- Автор задачи — автор курса, в котором она стояла. Название берём из первой
-- строки условия: до сих пор у задачи названия не было вовсе, а в списке банка
-- по чему-то надо ориентироваться. Если условие пустое (NOT NULL, но пробелы
-- допустимы) — подставляем название урока.
UPDATE `problems` p
  JOIN `steps`   s ON s.`id` = p.`id`
  JOIN `lessons` l ON l.`id` = s.`lesson_id`
  JOIN `modules` m ON m.`id` = l.`module_id`
  JOIN `courses` c ON c.`id` = m.`course_id`
   SET p.`author_id` = c.`author_id`,
       p.`title` = COALESCE(
           NULLIF(LEFT(TRIM(REPLACE(REPLACE(p.`description`, '\r', ' '), '\n', ' ')), 200), ''),
           LEFT(CONCAT('Задача из урока «', l.`title`, '»'), 200)
       );

ALTER TABLE `problems`
    MODIFY COLUMN `title`     VARCHAR(200) NOT NULL,
    MODIFY COLUMN `author_id` BIGINT(20)   NOT NULL,
    ADD CONSTRAINT `fk_problems_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`);

CREATE INDEX `ix_problems_author_visibility` ON `problems` (`author_id`, `visibility`);

CREATE TABLE `problem_tags` (
    `problem_id` BIGINT(20)  NOT NULL,
    `tag`        VARCHAR(50) NOT NULL,
    PRIMARY KEY (`problem_id`, `tag`),
    CONSTRAINT `fk_problem_tags_problem`
        FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Поиск по тегу идёт от тега к задачам, а первичный ключ начинается с
-- problem_id и для такого запроса бесполезен.
CREATE INDEX `ix_problem_tags_tag` ON `problem_tags` (`tag`);

-- --------------------------------------------------------------------
-- 3. problem_steps — размещение задачи в уроке
-- --------------------------------------------------------------------

CREATE TABLE `problem_steps` (
    `id`         BIGINT(20) NOT NULL,
    `problem_id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `ix_problem_steps_problem` (`problem_id`),
    CONSTRAINT `fk_problem_steps_step`
        FOREIGN KEY (`id`) REFERENCES `steps` (`id`),
    CONSTRAINT `fk_problem_steps_problem`
        FOREIGN KEY (`problem_id`) REFERENCES `problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- id задачи до этой миграции был идентификатором её шага — отсюда (id, id).
INSERT INTO `problem_steps` (`id`, `problem_id`)
SELECT `id`, `id` FROM `problems`;

-- --------------------------------------------------------------------
-- 4. Решение помнит и задачу, и место, где его отправили
-- --------------------------------------------------------------------

-- problem_id остаётся: история попыток и счётчики принадлежат задаче, а не
-- уроку. step_id нужен, чтобы отметить пройденным именно тот шаг, в котором
-- студент решал — задача может стоять в нескольких уроках.
--
-- Колонка допускает NULL и обнуляется при удалении размещения: снять задачу
-- с урока — теперь рядовое действие (в банке она остаётся), и оно не должно
-- упираться во внешний ключ от чужих решений. Само решение при этом не
-- теряется — оно по-прежнему привязано к задаче.
ALTER TABLE `submissions`
    ADD COLUMN `step_id` BIGINT(20) NULL;

UPDATE `submissions` SET `step_id` = `problem_id`;

ALTER TABLE `submissions`
    ADD CONSTRAINT `fk_submissions_step`
        FOREIGN KEY (`step_id`) REFERENCES `problem_steps` (`id`) ON DELETE SET NULL;
