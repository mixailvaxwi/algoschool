package com.algoschool.submission.checker;

import com.algoschool.problem.entity.ChoiceProblem;
import com.algoschool.problem.entity.CodeProblem;
import com.algoschool.problem.entity.TextProblem;
import com.algoschool.submission.entity.SubmissionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemCheckerTest {

    private final TextProblemChecker textChecker = new TextProblemChecker();
    private final ChoiceProblemChecker choiceChecker = new ChoiceProblemChecker();
    private final CodeProblemChecker codeChecker = new CodeProblemChecker();

    private TextProblem textProblem(String correctAnswer) {
        TextProblem p = new TextProblem();
        p.setCorrectAnswer(correctAnswer);
        return p;
    }

    private ChoiceProblem choiceProblem(Integer... correctIndexes) {
        ChoiceProblem p = new ChoiceProblem();
        p.setOptions(List.of("a", "b", "c"));
        p.setCorrectOptionIndexes(Set.of(correctIndexes));
        p.setMultipleChoice(correctIndexes.length > 1);
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
    void choiceCheckerAcceptsFullMultipleChoiceMatch() {
        assertThat(choiceChecker.check(choiceProblem(0, 2), "2,0")).isEqualTo(SubmissionStatus.CORRECT);
    }

    @Test
    void choiceCheckerRejectsPartialMultipleChoiceMatch() {
        assertThat(choiceChecker.check(choiceProblem(0, 2), "0")).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void choiceCheckerRejectsMultipleChoiceWithExtraOption() {
        assertThat(choiceChecker.check(choiceProblem(0, 2), "0,1,2")).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void choiceCheckerRejectsBlankPayload() {
        assertThat(choiceChecker.check(choiceProblem(1), " ")).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void codeCheckerDefersVerdictToExternalJudge() {
        assertThat(codeChecker.check(new CodeProblem(), "class Main {}")).isEqualTo(SubmissionStatus.PENDING);
    }

    /**
     * Теории в этом списке больше нет: чекер принимает Problem, а теория —
     * шаг урока, не задача, и до проверяющего модуля не доходит по типу.
     */
    @Test
    void checkersOnlySupportTheirOwnProblemType() {
        assertThat(textChecker.supports(new TextProblem())).isTrue();
        assertThat(textChecker.supports(new ChoiceProblem())).isFalse();
        assertThat(choiceChecker.supports(new ChoiceProblem())).isTrue();
        assertThat(choiceChecker.supports(new CodeProblem())).isFalse();
        assertThat(codeChecker.supports(new CodeProblem())).isTrue();
        assertThat(codeChecker.supports(new TextProblem())).isFalse();
    }
}
