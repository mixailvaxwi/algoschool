-- Новые типы задач и ручная проверка развёрнутых ответов.
--
-- До сих пор задача была одного из трёх типов, и все три проверялись
-- автоматически «всё или ничего». Добавляются четыре типа, два из которых
-- умеют частичный балл, и один, который автоматически не проверяется вовсе.
--
-- Ключевая мысль про хранение: правильный ответ не должен утекать студенту
-- через порядок элементов. Поэтому у задач на упорядочивание и соответствие
-- рядом с содержанием лежит перестановка — порядок показа, — а сам контент
-- хранится в правильном порядке, удобном для автора.

-- --------------------------------------------------------------------
-- 1. Число с допуском
-- --------------------------------------------------------------------

CREATE TABLE `numeric_problems` (
    `id`             BIGINT(20)  NOT NULL,
    `correct_value`  DOUBLE      NOT NULL,
    -- Допуск: либо абсолютный (|ответ - эталон| <= tolerance), либо
    -- относительный (доля от |эталона|). Нулевой допуск — точное совпадение.
    `tolerance`      DOUBLE      NOT NULL DEFAULT 0,
    `tolerance_kind` VARCHAR(16) NOT NULL DEFAULT 'ABSOLUTE',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_numeric_problems_problem`
        FOREIGN KEY (`id`) REFERENCES `problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------------------
-- 2. Соответствие: сопоставить левое правому
-- --------------------------------------------------------------------

CREATE TABLE `matching_problems` (
    `id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_matching_problems_problem`
        FOREIGN KEY (`id`) REFERENCES `problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Пары хранятся в правильном виде: left_items[i] соответствует right_items[i].
-- Автор так их и вводит — парами, ничего не перемешивая руками.
CREATE TABLE `matching_problem_left` (
    `problem_id` BIGINT(20)   NOT NULL,
    `position`   INT(11)      NOT NULL,
    `text`       VARCHAR(500) NOT NULL,
    PRIMARY KEY (`problem_id`, `position`),
    CONSTRAINT `fk_matching_left_problem`
        FOREIGN KEY (`problem_id`) REFERENCES `matching_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `matching_problem_right` (
    `problem_id` BIGINT(20)   NOT NULL,
    `position`   INT(11)      NOT NULL,
    `text`       VARCHAR(500) NOT NULL,
    PRIMARY KEY (`problem_id`, `position`),
    CONSTRAINT `fk_matching_right_problem`
        FOREIGN KEY (`problem_id`) REFERENCES `matching_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Перестановка правой колонки для показа студенту. Без неё правильный ответ
-- читался бы прямо из ответа API: i-й правый элемент подходит к i-му левому.
-- Задаётся один раз при сохранении задачи и наружу не отдаётся.
CREATE TABLE `matching_problem_display_order` (
    `problem_id`  BIGINT(20) NOT NULL,
    `position`    INT(11)    NOT NULL,
    `right_index` INT(11)    NOT NULL,
    PRIMARY KEY (`problem_id`, `position`),
    CONSTRAINT `fk_matching_display_problem`
        FOREIGN KEY (`problem_id`) REFERENCES `matching_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------------------
-- 3. Упорядочивание
-- --------------------------------------------------------------------

CREATE TABLE `ordering_problems` (
    `id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_ordering_problems_problem`
        FOREIGN KEY (`id`) REFERENCES `problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Элементы лежат в правильном порядке: автор вводит их так, как должно быть.
CREATE TABLE `ordering_problem_items` (
    `problem_id` BIGINT(20)   NOT NULL,
    `position`   INT(11)      NOT NULL,
    `text`       VARCHAR(500) NOT NULL,
    PRIMARY KEY (`problem_id`, `position`),
    CONSTRAINT `fk_ordering_items_problem`
        FOREIGN KEY (`problem_id`) REFERENCES `ordering_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ...а показываются перемешанными по этой перестановке — иначе задача решалась
-- бы чтением ответа API сверху вниз.
CREATE TABLE `ordering_problem_display_order` (
    `problem_id`  BIGINT(20) NOT NULL,
    `position`    INT(11)    NOT NULL,
    `item_index`  INT(11)    NOT NULL,
    PRIMARY KEY (`problem_id`, `position`),
    CONSTRAINT `fk_ordering_display_problem`
        FOREIGN KEY (`problem_id`) REFERENCES `ordering_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------------------
-- 4. Развёрнутый ответ — проверяется человеком
-- --------------------------------------------------------------------

CREATE TABLE `open_answer_problems` (
    `id`                BIGINT(20) NOT NULL,
    -- Критерии для проверяющего. Студенту не отдаются никогда.
    `review_guidelines` TEXT       DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_open_answer_problems_problem`
        FOREIGN KEY (`id`) REFERENCES `problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------------------
-- 5. Новые исходы проверки
-- --------------------------------------------------------------------

-- PARTIALLY_CORRECT — часть ответа верна: соответствие и упорядочивание
-- считают долю, и вердикт «неверно» при половине правильных пар вводил бы
-- студента в заблуждение.
-- PENDING_REVIEW — ждёт человека, а не проверяющей системы; опрос Ejudge
-- такие решения не подхватывает (он смотрит только PENDING).
--
-- Колонка создана Hibernate как MySQL ENUM, поэтому одного значения в
-- Java-перечислении мало: вставка падала бы с «Data truncated» (см. V4).
ALTER TABLE `submissions`
    MODIFY COLUMN `status` ENUM(
        'PENDING',
        'PENDING_REVIEW',
        'CORRECT',
        'PARTIALLY_CORRECT',
        'WRONG_ANSWER',
        'COMPILATION_ERROR',
        'TIME_LIMIT_EXCEEDED',
        'MEMORY_LIMIT_EXCEEDED',
        'RUNTIME_ERROR',
        'SUBMISSION_FAILED'
    ) NOT NULL;

-- --------------------------------------------------------------------
-- 6. След ручной проверки
-- --------------------------------------------------------------------

-- Кто проверил и что написал студенту. Балл у решения уже есть с V8 — здесь
-- добавляется только то, чего не бывает у автоматической проверки.
ALTER TABLE `submissions`
    ADD COLUMN `reviewed_by`     BIGINT(20)  NULL,
    ADD COLUMN `reviewed_at`     DATETIME(6) NULL,
    ADD COLUMN `review_comment`  TEXT        NULL,
    ADD CONSTRAINT `fk_submissions_reviewer`
        FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`);

-- Очередь проверки выбирает решения по статусу; индекс по статусу уже есть
-- с V2 (ix_submissions_status) и покрывает и этот запрос.
