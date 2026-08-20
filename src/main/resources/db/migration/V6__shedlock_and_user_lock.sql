-- Таблица блокировок ShedLock: не даёт двум инстансам приложения одновременно
-- опрашивать одни и те же решения в EjudgePoller. Схема — рекомендованная
-- shedlock-provider-jdbc-template для MySQL.
CREATE TABLE `shedlock` (
    `name`       VARCHAR(64)    NOT NULL,
    `lock_until` TIMESTAMP(3)   NOT NULL,
    `locked_at`  TIMESTAMP(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `locked_by`  VARCHAR(255)   NOT NULL,
    PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Блокировка аккаунта администратором. Раньше `isAccountNonLocked` в User
-- было жёстко зашито в true — блокировать было нечем.
ALTER TABLE `users`
    ADD COLUMN `locked` BIT(1) NOT NULL DEFAULT b'0';
