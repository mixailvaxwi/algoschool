package com.algoschool.submission.checker;

import com.algoschool.problem.entity.*;
import com.algoschool.submission.entity.SubmissionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemCheckerTest {

    private final TextProblemChecker textChecker = new TextProblemChecker();
    private final ChoiceProblemChecker choiceChecker = new ChoiceProblemChecker();
    private final CodeProblemChecker codeChecker = new CodeProblemChecker();
    private final NumericProblemChecker numericChecker = new NumericProblemChecker();
    private final OrderingProblemChecker orderingChecker = new OrderingProblemChecker();
    private final MatchingProblemChecker matchingChecker = new MatchingProblemChecker();
    private final OpenAnswerProblemChecker openChecker = new OpenAnswerProblemChecker();

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

    // --- Точный ввод --------------------------------------------------------

    @Test
    void textCheckerAcceptsExactAnswer() {
        assertThat(textChecker.check(textProblem("42"), "42").status()).isEqualTo(SubmissionStatus.CORRECT);
    }

    @Test
    void textCheckerIgnoresCaseAndSurroundingSpaces() {
        assertThat(textChecker.check(textProblem("Ответ"), "  ответ ").status()).isEqualTo(SubmissionStatus.CORRECT);
    }

    @Test
    void textCheckerRejectsWrongAnswer() {
        assertThat(textChecker.check(textProblem("42"), "43").status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    // --- Выбор варианта -----------------------------------------------------

    @Test
    void choiceCheckerAcceptsCorrectIndex() {
        assertThat(choiceChecker.check(choiceProblem(1), "1").status()).isEqualTo(SubmissionStatus.CORRECT);
    }

    @Test
    void choiceCheckerRejectsWrongIndex() {
        assertThat(choiceChecker.check(choiceProblem(1), "2").status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void choiceCheckerTreatsNonNumericPayloadAsWrongAnswer() {
        assertThat(choiceChecker.check(choiceProblem(1), "не число").status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void choiceCheckerAcceptsFullMultipleChoiceMatch() {
        assertThat(choiceChecker.check(choiceProblem(0, 2), "2,0").status()).isEqualTo(SubmissionStatus.CORRECT);
    }

    /**
     * Частичного балла у выбора варианта нет намеренно: иначе стратегия
     * «отметить всё» приносила бы очки за незнание.
     */
    @Test
    void choiceCheckerGivesNoPartialCreditForPartialMatch() {
        ChoiceProblem problem = choiceProblem(0, 2);
        problem.setMaxScore(10);

        CheckResult result = choiceChecker.check(problem, "0");

        assertThat(result.status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
        assertThat(result.score()).isZero();
    }

    @Test
    void choiceCheckerRejectsMultipleChoiceWithExtraOption() {
        assertThat(choiceChecker.check(choiceProblem(0, 2), "0,1,2").status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void choiceCheckerRejectsBlankPayload() {
        assertThat(choiceChecker.check(choiceProblem(1), " ").status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    // --- Код ----------------------------------------------------------------

    @Test
    void codeCheckerDefersVerdictToExternalJudge() {
        CheckResult result = codeChecker.check(new CodeProblem(), "class Main {}");

        assertThat(result.status()).isEqualTo(SubmissionStatus.PENDING);
        // Балла ещё нет — это не ноль: ноль пошёл бы в зачёт как неудача.
        assertThat(result.score()).isNull();
    }

    // --- Число с допуском ---------------------------------------------------

    private NumericProblem numericProblem(double value, double tolerance, ToleranceKind kind) {
        NumericProblem p = new NumericProblem();
        p.setCorrectValue(value);
        p.setTolerance(tolerance);
        p.setToleranceKind(kind);
        return p;
    }

    @Test
    void numericCheckerAcceptsExactValue() {
        assertThat(numericChecker.check(numericProblem(42, 0, ToleranceKind.ABSOLUTE), "42").status())
                .isEqualTo(SubmissionStatus.CORRECT);
    }

    /** Запятая — обычный десятичный разделитель на русской раскладке. */
    @Test
    void numericCheckerAcceptsCommaAsDecimalSeparator() {
        assertThat(numericChecker.check(numericProblem(3.14, 0.01, ToleranceKind.ABSOLUTE), "3,14").status())
                .isEqualTo(SubmissionStatus.CORRECT);
    }

    @Test
    void numericCheckerAcceptsValueOnTheEdgeOfTolerance() {
        assertThat(numericChecker.check(numericProblem(1.0, 0.1, ToleranceKind.ABSOLUTE), "1.1").status())
                .isEqualTo(SubmissionStatus.CORRECT);
    }

    @Test
    void numericCheckerRejectsValueOutsideTolerance() {
        assertThat(numericChecker.check(numericProblem(1.0, 0.1, ToleranceKind.ABSOLUTE), "1.2").status())
                .isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void numericCheckerAppliesRelativeTolerance() {
        // Один процент от 200 — это 2, поэтому 201 проходит, а 203 нет.
        NumericProblem problem = numericProblem(200, 0.01, ToleranceKind.RELATIVE);

        assertThat(numericChecker.check(problem, "201").status()).isEqualTo(SubmissionStatus.CORRECT);
        assertThat(numericChecker.check(problem, "203").status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void numericCheckerTreatsNonNumericAnswerAsWrong() {
        assertThat(numericChecker.check(numericProblem(42, 0, ToleranceKind.ABSOLUTE), "сорок два").status())
                .isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    // --- Упорядочивание -----------------------------------------------------

    /** Показанный порядок — обратный правильному: студент видит «в, б, а». */
    private OrderingProblem orderingProblem(int maxScore) {
        OrderingProblem p = new OrderingProblem();
        p.setItems(List.of("а", "б", "в"));
        p.setDisplayOrder(List.of(2, 1, 0));
        p.setMaxScore(maxScore);
        return p;
    }

    @Test
    void orderingCheckerAcceptsCorrectSequence() {
        // Правильный порядок «а, б, в» — это показанные элементы 2, 1, 0.
        CheckResult result = orderingChecker.check(orderingProblem(3), "2,1,0");

        assertThat(result.status()).isEqualTo(SubmissionStatus.CORRECT);
        assertThat(result.score()).isEqualTo(3);
    }

    /**
     * Одна позиция из трёх верна — доля 1/3 от трёх баллов даёт один балл.
     * Оценивать упорядочивание как «всё или ничего» было бы несоразмерно.
     */
    @Test
    void orderingCheckerGivesPartialCreditForPartialOrder() {
        CheckResult result = orderingChecker.check(orderingProblem(3), "2,0,1");

        assertThat(result.status()).isEqualTo(SubmissionStatus.PARTIALLY_CORRECT);
        assertThat(result.score()).isEqualTo(1);
    }

    /** На задаче в один балл дробить нечего: частичный ответ даёт ноль. */
    @Test
    void orderingCheckerCannotSplitASinglePoint() {
        CheckResult result = orderingChecker.check(orderingProblem(1), "2,0,1");

        assertThat(result.status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
        assertThat(result.score()).isZero();
    }

    @Test
    void orderingCheckerRejectsAnswerThatIsNotAPermutation() {
        assertThat(orderingChecker.check(orderingProblem(3), "0,0,1").status())
                .isEqualTo(SubmissionStatus.WRONG_ANSWER);
        assertThat(orderingChecker.check(orderingProblem(3), "0,1").status())
                .isEqualTo(SubmissionStatus.WRONG_ANSWER);
        assertThat(orderingChecker.check(orderingProblem(3), "мусор").status())
                .isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    // --- Соответствие -------------------------------------------------------

    /** Правая колонка показана в обратном порядке: «в», «б», «а». */
    private MatchingProblem matchingProblem(int maxScore) {
        MatchingProblem p = new MatchingProblem();
        p.setLeftItems(List.of("1", "2", "3"));
        p.setRightItems(List.of("а", "б", "в"));
        p.setRightDisplayOrder(List.of(2, 1, 0));
        p.setMaxScore(maxScore);
        return p;
    }

    @Test
    void matchingCheckerAcceptsAllCorrectPairs() {
        // Левый 0 — правый «а», показанный третьим (индекс 2), и так далее.
        CheckResult result = matchingChecker.check(matchingProblem(6), "0-2,1-1,2-0");

        assertThat(result.status()).isEqualTo(SubmissionStatus.CORRECT);
        assertThat(result.score()).isEqualTo(6);
    }

    @Test
    void matchingCheckerGivesPartialCreditPerPair() {
        // Верна только средняя пара: одна из трёх, то есть 2 балла из 6.
        CheckResult result = matchingChecker.check(matchingProblem(6), "0-0,1-1,2-2");

        assertThat(result.status()).isEqualTo(SubmissionStatus.PARTIALLY_CORRECT);
        assertThat(result.score()).isEqualTo(2);
    }

    /** Несопоставленные пары просто не приносят баллов. */
    @Test
    void matchingCheckerCountsOnlyAnsweredPairs() {
        CheckResult result = matchingChecker.check(matchingProblem(6), "0-2");

        assertThat(result.status()).isEqualTo(SubmissionStatus.PARTIALLY_CORRECT);
        assertThat(result.score()).isEqualTo(2);
    }

    /** Один левый элемент дважды — ответ противоречив, а не частично верен. */
    @Test
    void matchingCheckerRejectsContradictoryAnswer() {
        assertThat(matchingChecker.check(matchingProblem(6), "0-2,0-1").status())
                .isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void matchingCheckerRejectsMalformedAnswer() {
        assertThat(matchingChecker.check(matchingProblem(6), "0:2").status())
                .isEqualTo(SubmissionStatus.WRONG_ANSWER);
    }

    // --- Развёрнутый ответ --------------------------------------------------

    @Test
    void openAnswerCheckerSendsSubmissionToHumanReview() {
        CheckResult result = openChecker.check(new OpenAnswerProblem(), "Потому что так устроен алгоритм.");

        assertThat(result.status()).isEqualTo(SubmissionStatus.PENDING_REVIEW);
        assertThat(result.score()).isNull();
    }

    // --- Диспетчеризация ----------------------------------------------------

    /**
     * Теории в этом списке нет: чекер принимает Problem, а теория — шаг урока,
     * не задача, и до проверяющего модуля не доходит по типу.
     */
    @Test
    void checkersOnlySupportTheirOwnProblemType() {
        assertThat(textChecker.supports(new TextProblem())).isTrue();
        assertThat(textChecker.supports(new ChoiceProblem())).isFalse();
        assertThat(choiceChecker.supports(new ChoiceProblem())).isTrue();
        assertThat(choiceChecker.supports(new CodeProblem())).isFalse();
        assertThat(codeChecker.supports(new CodeProblem())).isTrue();
        assertThat(codeChecker.supports(new TextProblem())).isFalse();
        assertThat(numericChecker.supports(new NumericProblem())).isTrue();
        assertThat(numericChecker.supports(new TextProblem())).isFalse();
        assertThat(orderingChecker.supports(new OrderingProblem())).isTrue();
        assertThat(matchingChecker.supports(new MatchingProblem())).isTrue();
        assertThat(matchingChecker.supports(new OrderingProblem())).isFalse();
        assertThat(openChecker.supports(new OpenAnswerProblem())).isTrue();
        assertThat(openChecker.supports(new TextProblem())).isFalse();
    }
}
