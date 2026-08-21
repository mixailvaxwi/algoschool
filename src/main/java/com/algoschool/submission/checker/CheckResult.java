package com.algoschool.submission.checker;

import com.algoschool.submission.entity.SubmissionStatus;

/**
 * Исход проверки: вердикт и заработанный балл.
 * <p>
 * Балл появился здесь вместе с типами задач, которые умеют его дробить:
 * в соответствии и упорядочивании часть ответа может быть верной, и вердикт
 * «неверно» при половине правильных пар вводил бы студента в заблуждение.
 *
 * @param score {@code null} — балла ещё нет: проверка не закончена (PENDING)
 *              или ждёт человека (PENDING_REVIEW). Ноль — «проверено, не
 *              засчитано». Разница существенна: NULL не идёт в политику зачёта.
 */
public record CheckResult(SubmissionStatus status, Integer score) {

    public static CheckResult correct(int maxScore) {
        return new CheckResult(SubmissionStatus.CORRECT, maxScore);
    }

    public static CheckResult wrong() {
        return new CheckResult(SubmissionStatus.WRONG_ANSWER, 0);
    }

    /** Проверку выполнит Ejudge — вердикт придёт позже. */
    public static CheckResult pending() {
        return new CheckResult(SubmissionStatus.PENDING, null);
    }

    /** Проверку выполнит человек. */
    public static CheckResult pendingReview() {
        return new CheckResult(SubmissionStatus.PENDING_REVIEW, null);
    }

    /**
     * Балл по доле верного.
     * <p>
     * Доля меньше единицы округляется вниз: частично верный ответ не должен
     * получать полный балл из-за округления. На задаче в один балл это значит,
     * что частичного результата не существует, — так и есть, дробить нечего.
     */
    public static CheckResult of(double fraction, int maxScore) {
        if (fraction >= 1.0) {
            return correct(maxScore);
        }
        int score = (int) Math.floor(fraction * maxScore);
        return score <= 0
                ? wrong()
                : new CheckResult(SubmissionStatus.PARTIALLY_CORRECT, score);
    }
}
