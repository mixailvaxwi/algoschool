package com.mpanyavin.algoschool.module_assessment.service;

import com.mpanyavin.algoschool.module_assessment.dto.SubmissionRequest;
import com.mpanyavin.algoschool.module_assessment.dto.SubmissionResponse;
import com.mpanyavin.algoschool.module_assessment.entity.*;
import com.mpanyavin.algoschool.module_assessment.repository.ProblemOptionRepository;
import com.mpanyavin.algoschool.module_assessment.repository.ProblemRepository;
import com.mpanyavin.algoschool.module_assessment.repository.SubmissionRepository;
import com.mpanyavin.algoschool.module_assessment.repository.UserProblemSuccessRepository;
import com.mpanyavin.algoschool.module_user.entity.User;
import com.mpanyavin.algoschool.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final UserProblemSuccessRepository successRepository;

    // Внедряем новый репозиторий
    private final ProblemOptionRepository optionRepository;

    @Override
    @Transactional
    public SubmissionResponse submitSolution(Long userId, SubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Problem problem = problemRepository.findById(request.problemId())
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        // 1. Статистика: Это первая попытка студента решить эту задачу?
        boolean isFirstAttempt = !submissionRepository.existsByUserIdAndProblemId(userId, problem.getId());
        if (isFirstAttempt) {
            problemRepository.incrementAttemptedCount(problem.getId());
        }

        // 2. Проверка решения (используем Java 21 Pattern Matching для switch)
        SubmissionStatus status = switch (problem) {
            case InputProblem input -> checkInputProblem(input, request.payload());
            case ChoiceProblem choice -> checkChoiceProblem(choice, request.payload());
            case CodeProblem code -> checkCodeProblem(code, request.payload());
            default -> throw new IllegalStateException("Неизвестный тип задачи");
        };

        // 3. Создаем запись о попытке
        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .payload(request.payload())
                .status(status)
                .build();
        submissionRepository.save(submission);

        // 4. Геймификация: Если решено верно, начисляем XP (только если еще не решал)
        int xpEarned = 0;
        if (status == SubmissionStatus.CORRECT) {
            boolean alreadySolved = successRepository.existsByUserIdAndProblemId(userId, problem.getId());

            if (!alreadySolved) {
                xpEarned = problem.getXpReward();

                // Добавляем XP пользователю
                user.setTotalXp(user.getTotalXp() + xpEarned);
                userRepository.save(user);

                // Фиксируем успех
                UserProblemSuccess success = UserProblemSuccess.builder()
                        .user(user)
                        .problem(problem)
                        .earnedXp(xpEarned)
                        .build();
                successRepository.save(success);

                // Увеличиваем счетчик успешных решений у задачи
                problemRepository.incrementSuccessCount(problem.getId());
            }
        }

        return new SubmissionResponse(submission.getId(), status.name(), xpEarned);
    }

    // --- Реальная логика проверки ---

    private SubmissionStatus checkInputProblem(InputProblem problem, String payload) {
        if (payload == null || payload.isBlank()) {
            return SubmissionStatus.WRONG_ANSWER;
        }

        // Достаем все допустимые эталонные ответы для этой задачи
        List<ProblemOption> correctOptions = optionRepository.findByProblemIdAndIsCorrectTrue(problem.getId());
        String userAnswer = payload.trim();

        // Проверяем совпадение хотя бы с одним из эталонов
        for (ProblemOption option : correctOptions) {
            String correctAnswer = option.getText().trim();

            // Учитываем настройку чувствительности к регистру
            boolean isMatch = problem.getIsCaseSensitive()
                    ? correctAnswer.equals(userAnswer)
                    : correctAnswer.equalsIgnoreCase(userAnswer);

            if (isMatch) {
                return SubmissionStatus.CORRECT; // Нашли совпадение — ответ верный
            }
        }

        return SubmissionStatus.WRONG_ANSWER; // Совпадений не найдено
    }

    private SubmissionStatus checkChoiceProblem(ChoiceProblem problem, String payload) {
        if (payload == null || payload.isBlank()) {
            return SubmissionStatus.WRONG_ANSWER;
        }

        try {
            // 1. Превращаем ответ пользователя (например, "4, 7") в Set чисел (множество уникальных ID)
            Set<Long> userSelectedIds = Arrays.stream(payload.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());

            // 2. Защита "от дурака" или читера:
            // Если задача предполагает только один ответ, а прислали несколько
            if (!problem.getIsMultipleChoice() && userSelectedIds.size() > 1) {
                return SubmissionStatus.WRONG_ANSWER;
            }

            // 3. Достаем ID всех реально правильных вариантов из базы
            Set<Long> correctIds = optionRepository.findByProblemIdAndIsCorrectTrue(problem.getId())
                    .stream()
                    .map(ProblemOption::getId)
                    .collect(Collectors.toSet());

            // 4. Сравниваем множества.
            // Set.equals() вернет true ТОЛЬКО если размеры множеств совпадают
            // и они содержат абсолютно идентичные элементы.
            // Это решает проблему, когда студент выбрал 1 правильный и 1 неправильный ответ.
            if (userSelectedIds.equals(correctIds)) {
                return SubmissionStatus.CORRECT;
            } else {
                return SubmissionStatus.WRONG_ANSWER;
            }

        } catch (NumberFormatException e) {
            // Если фронтенд прислал мусор вместо чисел (например, "ответ 1")
            return SubmissionStatus.WRONG_ANSWER;
        }
    }

    private SubmissionStatus checkCodeProblem(CodeProblem problem, String payload) {
        // ... (Остается заглушкой для асинхронной системы проверки) ...
        return SubmissionStatus.PENDING;
    }
}

