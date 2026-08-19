package com.algoschool.ejudge;

import com.algoschool.submission.entity.SubmissionStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EjudgeVerdictMapperTest {

    private final EjudgeVerdictMapper mapper = new EjudgeVerdictMapper();

    @Test
    void mapsFinalVerdicts() {
        assertThat(mapper.toFinalStatus(0)).contains(SubmissionStatus.CORRECT);
        assertThat(mapper.toFinalStatus(1)).contains(SubmissionStatus.COMPILATION_ERROR);
        assertThat(mapper.toFinalStatus(2)).contains(SubmissionStatus.RUNTIME_ERROR);
        assertThat(mapper.toFinalStatus(3)).contains(SubmissionStatus.TIME_LIMIT_EXCEEDED);
        assertThat(mapper.toFinalStatus(5)).contains(SubmissionStatus.WRONG_ANSWER);
        assertThat(mapper.toFinalStatus(12)).contains(SubmissionStatus.MEMORY_LIMIT_EXCEEDED);
    }

    @Test
    void treatsInProgressCodesAsNotYetJudged() {
        assertThat(mapper.toFinalStatus(11)).isEmpty();  // Pending
        assertThat(mapper.toFinalStatus(16)).isEmpty();  // Pending review
        assertThat(mapper.toFinalStatus(98)).isEmpty();  // компиляция
    }

    /** Незнакомый код — повод спросить ещё раз, а не молча выставить вердикт. */
    @Test
    void treatsUnknownCodeAsNotYetJudged() {
        assertThat(mapper.toFinalStatus(777)).isEmpty();
        assertThat(mapper.toFinalStatus(null)).isEmpty();
    }
}
