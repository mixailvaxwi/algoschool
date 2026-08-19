-- Базовая схема AlgoSchool.
--
-- Снята с текущей схемы, которую до этого генерировал Hibernate ddl-auto=update,
-- поэтому существующие базы совпадают с ней байт в байт и проходят
-- spring.jpa.hibernate.ddl-auto=validate.
--
-- Для баз, созданных до подключения Flyway, включён baseline-on-migrate:
-- эта миграция помечается применённой без выполнения, а изменения приезжают
-- начиная с V2.
--
-- Порядок таблиц алфавитный (как в дампе), поэтому внешние ключи создаются
-- при выключенной проверке.

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `choice_problem_options` (
  `problem_id` bigint(20) NOT NULL,
  `option_text` varchar(255) DEFAULT NULL,
  KEY `FKpj5wcmmb98jtb03autw8xmeoj` (`problem_id`),
  CONSTRAINT `FKpj5wcmmb98jtb03autw8xmeoj` FOREIGN KEY (`problem_id`) REFERENCES `choice_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `choice_problems` (
  `correct_option_index` int(11) NOT NULL,
  `is_multiple_choice` bit(1) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKibbv2o3ddlf03k7mi7343gbxb` FOREIGN KEY (`id`) REFERENCES `step_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `code_problems` (
  `allowed_languages` varchar(255) DEFAULT NULL,
  `ejudge_contest_id` int(11) DEFAULT NULL,
  `ejudge_problem_id` varchar(255) DEFAULT NULL,
  `memory_limit_mb` int(11) DEFAULT NULL,
  `time_limit_sec` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKhj5mntuhinbbb045107pp0fhq` FOREIGN KEY (`id`) REFERENCES `step_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `course_applications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `motivation_message` text DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') NOT NULL,
  `course_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjiyy1pc4j07mvgk31a6pi432` (`course_id`),
  KEY `FKq2274h2hutblxbm3l7cyoqlnv` (`user_id`),
  CONSTRAINT `FKjiyy1pc4j07mvgk31a6pi432` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
  CONSTRAINT `FKq2274h2hutblxbm3l7cyoqlnv` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `courses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `access_type` varchar(255) NOT NULL DEFAULT 'OPEN',
  `description` text DEFAULT NULL,
  `is_published` bit(1) NOT NULL,
  `title` varchar(255) NOT NULL,
  `author_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhbo41uaq9qyi5ora71hq2oyah` (`author_id`),
  CONSTRAINT `FKhbo41uaq9qyi5ora71hq2oyah` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `lessons` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_index` int(11) NOT NULL,
  `title` varchar(150) NOT NULL,
  `module_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt9yjhjbd9y3w6fxs66ny1wu02` (`module_id`),
  CONSTRAINT `FKt9yjhjbd9y3w6fxs66ny1wu02` FOREIGN KEY (`module_id`) REFERENCES `modules` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `modules` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `position_index` int(11) DEFAULT NULL,
  `title` varchar(150) NOT NULL,
  `course_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8qnnp812q1jd38fx7mxrhpw9` (`course_id`),
  CONSTRAINT `FK8qnnp812q1jd38fx7mxrhpw9` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `step_file_problems` (
  `allowed_extensions` varchar(255) DEFAULT NULL,
  `max_file_size_mb` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKhjm6bww1t1xe5cmmxaspiqsy0` FOREIGN KEY (`id`) REFERENCES `step_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `step_problems` (
  `attempted_students_count` int(11) NOT NULL,
  `description` text NOT NULL,
  `success_students_count` int(11) NOT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK5pgugsm9nnvitihqie13ff54m` FOREIGN KEY (`id`) REFERENCES `steps` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `steps` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `position_index` int(11) NOT NULL,
  `lesson_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdwmt3d90j4m1ik9jooo3g2plq` (`lesson_id`),
  CONSTRAINT `FKdwmt3d90j4m1ik9jooo3g2plq` FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `submissions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `compiler_output` text DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `external_run_id` int(11) DEFAULT NULL,
  `payload` text NOT NULL,
  `status` enum('COMPILATION_ERROR','CORRECT','MEMORY_LIMIT_EXCEEDED','PENDING','RUNTIME_ERROR','TIME_LIMIT_EXCEEDED','WRONG_ANSWER') NOT NULL,
  `test_results_json` text DEFAULT NULL,
  `problem_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKaqam0ndkg4uxbfmftvfpe84mm` (`problem_id`),
  KEY `FK760bgu69957phd7hax608jdms` (`user_id`),
  CONSTRAINT `FK760bgu69957phd7hax608jdms` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKaqam0ndkg4uxbfmftvfpe84mm` FOREIGN KEY (`problem_id`) REFERENCES `step_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `text_problems` (
  `correct_answer` varchar(255) NOT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKdxmkyr3dj2hgkgfukvdy5gi1f` FOREIGN KEY (`id`) REFERENCES `step_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `theory_steps` (
  `content` text NOT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK7qq3r7lvf9808xa9f5dfmui67` FOREIGN KEY (`id`) REFERENCES `steps` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `user_courses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `enrolled_at` datetime(6) NOT NULL,
  `course_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb84hga2qpwc4vv44lmyb8mwux` (`course_id`),
  KEY `FK5i2mwg17kvpk92fy6cdii93da` (`user_id`),
  CONSTRAINT `FK5i2mwg17kvpk92fy6cdii93da` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKb84hga2qpwc4vv44lmyb8mwux` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `user_problem_successes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `earned_xp` int(11) NOT NULL,
  `solved_at` datetime(6) NOT NULL,
  `problem_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl6oq039wxqnkbrjr6vv31shty` (`problem_id`),
  KEY `FKgtvxyvocs9jco0xjhy4e6ryq3` (`user_id`),
  CONSTRAINT `FKgtvxyvocs9jco0xjhy4e6ryq3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKl6oq039wxqnkbrjr6vv31shty` FOREIGN KEY (`problem_id`) REFERENCES `step_problems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `user_step_progress` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `completed_at` datetime(6) NOT NULL,
  `is_completed` bit(1) NOT NULL,
  `submitted_payload` text DEFAULT NULL,
  `step_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqfy5wb1wkvdgtoona0tuwuxql` (`step_id`),
  KEY `FKg9bd3m3bqxpcxwowhndq0ha6t` (`user_id`),
  CONSTRAINT `FKg9bd3m3bqxpcxwowhndq0ha6t` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqfy5wb1wkvdgtoona0tuwuxql` FOREIGN KEY (`step_id`) REFERENCES `steps` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('ROLE_ADMIN','ROLE_STUDENT','ROLE_TEACHER') NOT NULL,
  `username` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
SET FOREIGN_KEY_CHECKS = 1;
