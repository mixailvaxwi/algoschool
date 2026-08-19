package com.algoschool.submission.checker;

import com.algoschool.step.entity.ChoiceProblem;
import com.algoschool.step.entity.CodeProblem;
import com.algoschool.step.entity.TextProblem;
import com.algoschool.step.entity.TheoryStep;
import com.algoschool.submission.entity.SubmissionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StepCheckerTest {

    private final TextProblemChecker textChecker = new TextProblemChecker();
    private final ChoiceProblemChecker choiceChecker = new ChoiceProblemChecker();
    private final CodeProblemChecker codeChecker = new CodeProblemChecker();

    private TextProblem textProblem(String correctAnswer) {
        TextProblem p = new TextProblem();
        p.setCorrectAnswer(correctAnswer);
        return p;
    }

    private ChoiceProblem choiceProblem(int correctIndex) {
        ChoiceProblem p = new ChoiceProblem();
        p.setOptions(List.of("a", "b", "c"));
        p.setCorrectOptionIndex(correctIndex);
        return p;
    }

    @Test
    void textCheckerAcceptsExactAnswer() {
        assertThat(textChecker.check(textProblem("42"), "42")).isEqualTo(SubmissionStatus.CORRECT);
    }

    @Test
    void textCheckerIgnoresCaseAndSurroundingSpaces() {
        assertThat(textChecker.check(textProblem("Ответ"), "  ответ ")).isEqualTo(SubmissionStatus.CORRECT);
    }

    @Test
    void textCheckerRejectsWrongAnswer() {
        assertThat(textChecker.check(textProblem("42"), "43")).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void choiceCheckerAcceptsCorrectIndex() {
        assertThat(choiceChecker.check(choiceProblem(1), "1")).isEqualTo(SubmissionStatus.CORRECT);
    }

    @Test
    void choiceCheckerRejectsWrongIndex() {
        assertThat(choiceChecker.check(choiceProblem(1), "2")).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void choiceCheckerTreatsNonNumericPayloadAsWrongAnswer() {
        assertThat(choiceChecker.check(choiceProblem(1), "не число")).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void codeCheckerDefersVerdictToExternalJudge() {
        assertThat(codeChecker.check(new CodeProblem(), "class Main {}")).isEqualTo(SubmissionStatus.PENDING);
    }

    @Test
    void checkersOnlySupportTheirOwnStepType() {
        assertThat(textChecker.supports(new TextProblem())).isTrue();
        assertThat(textChecker.supports(new ChoiceProblem())).isFalse();
        assertThat(choiceChecker.supports(new ChoiceProblem())).isTrue();
        assertThat(codeChecker.supports(new CodeProblem())).isTrue();
        assertThat(codeChecker.supports(new TheoryStep())).isFalse();
    }
}
