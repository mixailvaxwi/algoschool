package com.algoschool.submission.checker;

import com.algoschool.problem.entity.MatchingProblem;
import com.algoschool.problem.entity.Problem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MatchingProblemChecker implements ProblemChecker {

    @Override
    public boolean supports(Problem problem) {
        return problem instanceof MatchingProblem;
    }

    /**
     * Ответ — пары «левый индекс-показанный правый индекс» через запятую:
     * «0-2,1-0,2-1».
     * <p>
     * Балл — доля верно сопоставленных пар: студент, угадавший три пары из
     * четырёх, знает предмет лучше того, кто не угадал ни одной, и вердикт
     * должен это различать.
     */
    @Override
    public CheckResult check(Problem problem, String payload) {
        MatchingProblem matching = (MatchingProblem) problem;
        List<Integer> displayOrder = matching.getRightDisplayOrder();
        int size = matching.getLeftItems().size();

        Map<Integer, Integer> answer = Payloads.parsePairs(payload);
        if (answer == null || size == 0) {
            return CheckResult.wrong();
        }

        int matched = 0;
        for (int left = 0; left < size; left++) {
            Integer shownRight = answer.get(left);
            if (shownRight == null || shownRight < 0 || shownRight >= displayOrder.size()) {
                continue;
            }
            // Показанный правый элемент под индексом shownRight — это исходный
            // элемент displayOrder.get(shownRight); пара верна, когда он стоит
            // против своего левого.
            if (displayOrder.get(shownRight) == left) {
                matched++;
            }
        }

        return CheckResult.of((double) matched / size, matching.getMaxScore());
    }
}
