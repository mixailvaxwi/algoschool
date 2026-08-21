package com.algoschool.submission.checker;

import com.algoschool.problem.entity.OrderingProblem;
import com.algoschool.problem.entity.Problem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderingProblemChecker implements ProblemChecker {

    @Override
    public boolean supports(Problem problem) {
        return problem instanceof OrderingProblem;
    }

    /**
     * Ответ — перестановка показанных индексов: «2,0,1» значит, что студент
     * поставил на первое место элемент, который видел третьим.
     * <p>
     * Балл — доля элементов, оказавшихся на своём месте. Оценивать
     * упорядочивание как «всё или ничего» было бы несоразмерно: одна
     * переставленная пара обнуляла бы верно собранный остаток.
     */
    @Override
    public CheckResult check(Problem problem, String payload) {
        OrderingProblem ordering = (OrderingProblem) problem;
        List<Integer> displayOrder = ordering.getDisplayOrder();
        int size = displayOrder.size();

        List<Integer> answer = Payloads.parseIndexes(payload);
        if (answer == null || answer.size() != size || !Payloads.isPermutation(answer, size)) {
            // Не перестановка — ответ испорчен по дороге либо собран не тем
            // клиентом; засчитывать по частям такое нельзя.
            return CheckResult.wrong();
        }

        int inPlace = 0;
        for (int position = 0; position < size; position++) {
            // На месте position студент поставил элемент, который видел под
            // индексом answer.get(position); в исходном (правильном) порядке
            // это элемент displayOrder.get(...).
            if (displayOrder.get(answer.get(position)) == position) {
                inPlace++;
            }
        }

        return CheckResult.of(size == 0 ? 1.0 : (double) inPlace / size, ordering.getMaxScore());
    }
}
