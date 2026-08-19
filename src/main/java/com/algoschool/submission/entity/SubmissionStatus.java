package com.algoschool.submission.entity;

public enum SubmissionStatus {
    PENDING,                // В очереди на проверку (важно для асинхронной проверки кода)
    CORRECT,                // Полностью верное решение (Accepted)
    WRONG_ANSWER,           // Неверный ответ (WA)
    COMPILATION_ERROR,      // Ошибка компиляции (CE)
    TIME_LIMIT_EXCEEDED,    // Превышен лимит времени (TLE)
    MEMORY_LIMIT_EXCEEDED,  // Превышен лимит памяти (MLE)
    RUNTIME_ERROR           // Ошибка во время выполнения (RE)
}