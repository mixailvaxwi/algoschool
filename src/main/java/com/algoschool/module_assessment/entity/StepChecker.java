package com.algoschool.module_assessment.entity;

public interface StepChecker {
    // Проверяет, подходит ли этот чекер для данного типа шага
    boolean supports(Step step);

    // Возвращает true, если ответ верный
    boolean check(Step step, String userPayload);
}