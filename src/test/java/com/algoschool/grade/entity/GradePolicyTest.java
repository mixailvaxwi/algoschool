package com.algoschool.grade.entity;

import com.algoschool.exception.AppException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Правила зачёта. Считают в долях от максимума попытки, а не в баллах: вес
 * элемента в курсе преподаватель может поменять уже после того, как студенты
 * нарешали попыток, и доля такую смену переживает.
 */
class GradePolicyTest {

    /** Попытки идут от старой к новой: сначала мимо, потом в точку, потом снова мимо. */
    private static final List<Double> ATTEMPTS = List.of(0.0, 1.0, 0.5);

    @Test
    void bestTakesHighestAttempt() {
        assertThat(GradePolicy.BEST.apply(ATTEMPTS).getAsDouble()).isEqualTo(1.0);
    }

    @Test
    void lastTakesMostRecentAttempt() {
        assertThat(GradePolicy.LAST.apply(ATTEMPTS).getAsDouble()).isEqualTo(0.5);
    }

    @Test
    void firstTakesEarliestAttempt() {
        assertThat(GradePolicy.FIRST.apply(ATTEMPTS).getAsDouble()).isEqualTo(0.0);
    }

    @Test
    void averageTakesMean() {
        assertThat(GradePolicy.AVERAGE.apply(ATTEMPTS).getAsDouble()).isCloseTo(0.5, within(1e-9));
    }

    /**
     * Пусто — это не ноль. Ноль означает «проверено, не засчитано», а отсутствие
     * попыток означает «не сдавал», и в журнале это прочерк, а не двойка.
     */
    @Test
    void everyPolicyReturnsNothingWithoutAttempts() {
        for (GradePolicy policy : GradePolicy.values()) {
            assertThat(policy.apply(List.of())).isEmpty();
        }
    }

    @Test
    void singleAttemptIsCreditedByEveryPolicy() {
        for (GradePolicy policy : GradePolicy.values()) {
            assertThat(policy.apply(List.of(0.75)).getAsDouble()).isCloseTo(0.75, within(1e-9));
        }
    }

    @Test
    void rejectsUnknownPolicyName() {
        assertThatThrownBy(() -> GradePolicy.parse("HIGHEST"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Неизвестная политика зачёта");
    }
}
