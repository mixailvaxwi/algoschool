-- Баллы вместо «решено / не решено».
--
-- До сих пор успех был булевым: UserStepProgress.is_completed, и всё. Ни вес
-- задачи в курсе, ни частичный балл, ни журнал оценок при этом невозможны —
-- баллы негде хранить.
--
-- Появляются три вещи:
--   submissions.score / max_score — что заработала конкретная отправка;
--   grade_items                   — оцениваемый элемент курса (размещённая
--                                   задача либо ручной столбец журнала);
--   grades                        — зачтённый балл за элемент, посчитанный по
--                                   политике зачёта (лучшая попытка и т. п.).
--
-- Разделение «отправка / зачтённый балл» существенно: попыток много, зачёт
-- один, и правило зачёта задаёт преподаватель.
--
-- Порядок как в V2 и V7: сначала данные, потом ограничения.

-- --------------------------------------------------------------------
-- 1. Балл за отправку
-- --------------------------------------------------------------------

-- NULL, а не 0, и это разные вещи. Ноль — это «проверено, не засчитано»;
-- NULL — «балла ещё нет»: у PENDING проверка не закончена, а SUBMISSION_FAILED
-- вообще не вердикт по ответу студента, а сбой Ejudge. Ни то, ни другое не
-- должно участвовать в политике зачёта наравне с проверенными попытками —
-- иначе недоступность проверяющей системы портила бы оценку.
ALTER TABLE `submissions`
    ADD COLUMN `score`     INT(11) NULL,
    ADD COLUMN `max_score` INT(11) NULL;

-- Прошлые решения оцениваем по вердикту: до этой миграции других данных нет.
UPDATE `submissions` s
  JOIN `problems` p ON p.`id` = s.`problem_id`
   SET s.`max_score` = p.`max_score`,
       s.`score` = IF(s.`status` = 'CORRECT', p.`max_score`, 0)
 WHERE s.`status` NOT IN ('PENDING', 'SUBMISSION_FAILED');

-- --------------------------------------------------------------------
-- 2. Оцениваемые элементы курса
-- --------------------------------------------------------------------

-- max_score здесь — вес элемента в курсе, а не свойство задачи: одна и та же
-- задача из банка может стоить в разных курсах по-разному, и балл отправки
-- пересчитывается в шкалу элемента пропорционально.
CREATE TABLE `grade_items` (
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT,
    `course_id`   BIGINT(20)   NOT NULL,
    `kind`        VARCHAR(16)  NOT NULL,
    `step_id`     BIGINT(20)   NULL,
    `title`       VARCHAR(200) NOT NULL,
    `max_score`   INT(11)      NOT NULL,
    `policy`      VARCHAR(16)  NOT NULL,
    `order_index` INT(11)      NOT NULL,
    PRIMARY KEY (`id`),
    -- Одно размещение задачи — ровно один столбец журнала.
    UNIQUE KEY `uq_grade_items_step` (`step_id`),
    KEY `ix_grade_items_course` (`course_id`, `order_index`),
    CONSTRAINT `fk_grade_items_course`
        FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
    CONSTRAINT `fk_grade_items_step`
        FOREIGN KEY (`step_id`) REFERENCES `problem_steps` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Для каждой уже размещённой задачи заводим элемент журнала. Порядок столбцов
-- повторяет порядок прохождения курса: модуль, урок, шаг.
INSERT INTO `grade_items` (`course_id`, `kind`, `step_id`, `title`, `max_score`, `policy`, `order_index`)
SELECT c.`id`,
       'PROBLEM',
       ps.`id`,
       p.`title`,
       p.`max_score`,
       'BEST',
       ROW_NUMBER() OVER (
           PARTITION BY c.`id`
           ORDER BY m.`position_index`, l.`order_index`, s.`order_index`, s.`id`
       )
  FROM `problem_steps` ps
  JOIN `problems` p ON p.`id` = ps.`problem_id`
  JOIN `steps`    s ON s.`id` = ps.`id`
  JOIN `lessons`  l ON l.`id` = s.`lesson_id`
  JOIN `modules`  m ON m.`id` = l.`module_id`
  JOIN `courses`  c ON c.`id` = m.`course_id`;

-- --------------------------------------------------------------------
-- 3. Зачтённые баллы
-- --------------------------------------------------------------------

CREATE TABLE `grades` (
    `id`            BIGINT(20)  NOT NULL AUTO_INCREMENT,
    `grade_item_id` BIGINT(20)  NOT NULL,
    `user_id`       BIGINT(20)  NOT NULL,
    `score`         INT(11)     NOT NULL,
    -- Ручную оценку пересчёт не трогает: иначе следующая отправка студента
    -- молча стёрла бы решение преподавателя.
    `is_manual`     BIT(1)      NOT NULL DEFAULT b'0',
    `comment`       TEXT        NULL,
    `graded_by`     BIGINT(20)  NULL,
    `updated_at`    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_grades_item_user` (`grade_item_id`, `user_id`),
    KEY `ix_grades_user` (`user_id`),
    CONSTRAINT `fk_grades_item`
        FOREIGN KEY (`grade_item_id`) REFERENCES `grade_items` (`id`),
    CONSTRAINT `fk_grades_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_grades_graded_by`
        FOREIGN KEY (`graded_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Переносим накопленную историю по политике BEST, с которой заведены все
-- элементы выше. Балл считается по задаче, а не по размещению: задача из банка
-- может стоять в нескольких уроках, и решение засчитывается везде, где она
-- стоит, — так же, как отметка о прохождении (см. V7).
--
-- Зачитываем только записанным на курс: журнал курса не должен показывать
-- баллы человека, который на него не ходит.
-- Пересчёта в шкалу элемента здесь не нужно: элементы только что заведены с
-- max_score задачи, то есть шкалы совпадают. Расхождение появится позже, когда
-- преподаватель поменяет вес элемента, — этим занимается уже код.
INSERT INTO `grades` (`grade_item_id`, `user_id`, `score`, `is_manual`, `updated_at`)
SELECT gi.`id`,
       sub.`user_id`,
       MAX(sub.`score`),
       b'0',
       NOW(6)
  FROM `grade_items` gi
  JOIN `problem_steps` ps ON ps.`id` = gi.`step_id`
  JOIN `submissions` sub  ON sub.`problem_id` = ps.`problem_id`
                         AND sub.`score` IS NOT NULL
  JOIN `user_courses` uc  ON uc.`user_id` = sub.`user_id`
                         AND uc.`course_id` = gi.`course_id`
 GROUP BY gi.`id`, sub.`user_id`;
