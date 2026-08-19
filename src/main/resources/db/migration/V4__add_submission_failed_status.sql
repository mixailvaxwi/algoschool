-- Новый статус SUBMISSION_FAILED: решение не удалось передать в Ejudge.
-- Это сбой инфраструктуры, а не вердикт по коду студента (раньше в таком
-- случае записывался RUNTIME_ERROR).
--
-- Колонка создана Hibernate как MySQL ENUM с перечислением констант, поэтому
-- одного лишь добавления значения в Java-перечисление недостаточно: вставка
-- падала бы с «Data truncated for column 'status'». Обратите внимание, что
-- ddl-auto=validate такое расхождение не ловит — список значений ENUM он
-- не сверяет.

ALTER TABLE submissions
    MODIFY COLUMN `status` ENUM(
        'PENDING',
        'CORRECT',
        'WRONG_ANSWER',
        'COMPILATION_ERROR',
        'TIME_LIMIT_EXCEEDED',
        'MEMORY_LIMIT_EXCEEDED',
        'RUNTIME_ERROR',
        'SUBMISSION_FAILED'
    ) NOT NULL;
