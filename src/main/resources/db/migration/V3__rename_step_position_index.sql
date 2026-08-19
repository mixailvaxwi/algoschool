-- Шаг урока хранил порядок в position_index, а урок и вся публичная часть API —
-- в orderIndex. Из-за расхождения фронтенд сортировал шаги по несуществующему
-- полю: компаратор возвращал NaN, и шаги показывались в произвольном порядке.
--
-- Приводим шаг к тому же имени, что у урока: поле, колонка и JSON — orderIndex.

ALTER TABLE steps
    CHANGE COLUMN `position_index` `order_index` INT NOT NULL;
