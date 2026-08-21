package com.algoschool.submission.checker;

import com.algoschool.problem.entity.NumericProblem;
import com.algoschool.problem.entity.Problem;
import com.algoschool.problem.entity.ToleranceKind;
import org.springframework.stereotype.Component;

@Component
public class NumericProblemChecker implements ProblemChecker {

    @Override
    public boolean supports(Problem problem) {
        return problem instanceof NumericProblem;
    }

    @Override
    public CheckResult check(Problem problem, String payload) {
        NumericProblem numeric = (NumericProblem) problem;

        Double answer = parse(payload);
        if (answer == null) {
            return CheckResult.wrong();
        }

        double expected = numeric.getCorrectValue();
        double tolerance = numeric.getTolerance() == null ? 0.0 : numeric.getTolerance();
        double allowed = numeric.getToleranceKind() == ToleranceKind.RELATIVE
                ? Math.abs(expected) * tolerance
                : tolerance;

        // Допуск включительно: «± 0.01» должно принимать ровно 0.01 отклонения.
        // Сравнение с крохотной добавкой — иначе двоичное представление вроде
        // 0.1 + 0.2 отбрасывало бы ответ, попадающий в допуск ровно по краю.
        boolean withinTolerance = Math.abs(answer - expected) <= allowed + 1e-9;

        return withinTolerance ? CheckResult.correct(numeric.getMaxScore()) : CheckResult.wrong();
    }

    /**
     * Разбор ответа студента.
     * <p>
     * Запятая как десятичный разделитель принимается наравне с точкой: на
     * русской раскладке её набирают чаще, и отвергать «3,14» как «не число»
     * значило бы проверять раскладку, а не знание.
     */
    private Double parse(String payload) {
        if (payload == null) {
            return null;
        }
        String normalized = payload.trim().replace(',', '.').replace(" ", "");
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(normalized);
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
