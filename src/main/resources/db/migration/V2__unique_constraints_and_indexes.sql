-- Ограничения целостности, которых не было.
--
-- Все проверки «уже записан / уже подал заявку» были реализованы как
-- read-then-write без поддержки со стороны БД: два параллельных запроса
-- проходили проверку одновременно и создавали дубли. Здесь добавляется
-- недостающий барьер.
--
-- ВНИМАНИЕ: перед созданием ограничений удаляются уже накопившиеся дубли.
-- Правила выбора «выжившей» строки описаны у каждого блока.

-- --------------------------------------------------------------------
-- 1. Записи на курс: один пользователь — одна запись на курс
-- --------------------------------------------------------------------

-- Оставляем самую раннюю запись (минимальный id) — она отражает момент,
-- когда студент реально получил доступ.
DELETE a FROM user_courses a
JOIN user_courses b
  ON a.user_id = b.user_id
 AND a.course_id = b.course_id
 AND b.id < a.id;

ALTER TABLE user_courses
    ADD CONSTRAINT uq_user_courses_user_course UNIQUE (user_id, course_id);

-- --------------------------------------------------------------------
-- 2. Прогресс по шагам: одна строка на пару (пользователь, шаг)
-- --------------------------------------------------------------------

-- Оставляем «лучшую» строку: сначала пройденные, среди них — самую свежую,
-- при полном равенстве — с меньшим id. Так отметка о прохождении не теряется.
DELETE p FROM user_step_progress p
JOIN user_step_progress q
  ON p.user_id = q.user_id
 AND p.step_id = q.step_id
 AND (
        q.is_completed + 0 > p.is_completed + 0
     OR (q.is_completed + 0 = p.is_completed + 0 AND q.completed_at > p.completed_at)
     OR (q.is_completed + 0 = p.is_completed + 0 AND q.completed_at = p.completed_at AND q.id < p.id)
 );

ALTER TABLE user_step_progress
    ADD CONSTRAINT uq_user_step_progress_user_step UNIQUE (user_id, step_id);

-- --------------------------------------------------------------------
-- 3. Заявки на курс: не более одной заявки в статусе PENDING
-- --------------------------------------------------------------------

-- Простой UNIQUE (user_id, course_id) здесь не подходит: после отказа студент
-- должен иметь возможность подать заявку заново, и прежние заявки остаются
-- в истории. Ограничиваем только одновременно висящие заявки — через
-- генерируемый столбец, который не NULL лишь для PENDING (в уникальном
-- индексе MariaDB допускает сколько угодно NULL).

DELETE a FROM course_applications a
JOIN course_applications b
  ON a.user_id = b.user_id
 AND a.course_id = b.course_id
 AND a.status = 'PENDING'
 AND b.status = 'PENDING'
 AND b.id < a.id;

ALTER TABLE course_applications
    ADD COLUMN pending_uniq VARCHAR(64)
        GENERATED ALWAYS AS (
            CASE WHEN status = 'PENDING' THEN CONCAT(user_id, ':', course_id) END
        ) STORED;

ALTER TABLE course_applications
    ADD CONSTRAINT uq_course_applications_pending UNIQUE (pending_uniq);

-- Составной индекс под выборку «заявки этого студента по этому курсу»
-- (по отдельности индексы на user_id и course_id уже есть от внешних ключей).
CREATE INDEX ix_course_applications_user_course
    ON course_applications (user_id, course_id);

-- --------------------------------------------------------------------
-- 4. Индексы под фактические запросы
-- --------------------------------------------------------------------

-- История попыток студента по задаче: фильтр по паре + сортировка по дате.
CREATE INDEX ix_submissions_user_problem_created
    ON submissions (user_id, problem_id, created_at);

-- Опрос ожидающих решений планировщиком раз в 3 секунды.
CREATE INDEX ix_submissions_status
    ON submissions (status);

-- Каталог показывает только опубликованные курсы.
CREATE INDEX ix_courses_is_published
    ON courses (is_published);
