package com.algoschool.ejudge;

import com.algoschool.submission.entity.SubmissionStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Перевод числовых вердиктов Ejudge в статусы платформы.
 * <p>
 * Раньше это был switch с голыми числами и условие
 * {@code status <= 95 && status != 11 && status != 16}, по которому нельзя было
 * понять, какие значения считаются окончательными. Таблицы ниже делают набор
 * явным: всё, чего в них нет, считается ещё не проверенным и опрашивается снова.
 */
@Component
public class EjudgeVerdictMapper {

    /** Окончательные вердикты Ejudge. */
    private static final Map<Integer, SubmissionStatus> FINAL_VERDICTS = Map.of(
            0,  SubmissionStatus.CORRECT,               // OK
            1,  SubmissionStatus.COMPILATION_ERROR,     // CE
            2,  SubmissionStatus.RUNTIME_ERROR,         // RT
            3,  SubmissionStatus.TIME_LIMIT_EXCEEDED,   // TL
            4,  SubmissionStatus.WRONG_ANSWER,          // PE (presentation error)
            5,  SubmissionStatus.WRONG_ANSWER,          // WA
            6,  SubmissionStatus.WRONG_ANSWER,          // CF (check failed)
            7,  SubmissionStatus.WRONG_ANSWER,          // PT (partial solution)
            9,  SubmissionStatus.WRONG_ANSWER,          // IG (ignored)
            12, SubmissionStatus.MEMORY_LIMIT_EXCEEDED  // ML
    );

    /**
     * Промежуточные состояния: решение ещё в работе, вердикт спрашиваем позже.
     * 11 — Pending, 16 — Pending review, 96..99 — компиляция и прогон тестов.
     */
    private static final Set<Integer> IN_PROGRESS = Set.of(11, 16, 96, 97, 98, 99);

    /**
     * @return окончательный статус, либо пусто — если проверка ещё идёт
     *         или код вердикта незнаком (тогда безопаснее спросить ещё раз).
     */
    public Optional<SubmissionStatus> toFinalStatus(Integer ejudgeStatus) {
        if (ejudgeStatus == null || IN_PROGRESS.contains(ejudgeStatus)) {
            return Optional.empty();
        }
        return Optional.ofNullable(FINAL_VERDICTS.get(ejudgeStatus));
    }
}
