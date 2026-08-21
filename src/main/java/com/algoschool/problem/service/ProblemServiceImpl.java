package com.algoschool.problem.service;

import com.algoschool.exception.AppException;
import com.algoschool.problem.dto.ProblemDto;
import com.algoschool.problem.dto.ProblemRequest;
import com.algoschool.problem.dto.ProblemSearchQuery;
import com.algoschool.problem.entity.Difficulty;
import com.algoschool.problem.entity.Problem;
import com.algoschool.problem.entity.ProblemType;
import com.algoschool.problem.entity.ProblemVisibility;
import com.algoschool.problem.repository.ProblemRepository;
import com.algoschool.step.repository.ProblemStepRepository;
import com.algoschool.submission.repository.SubmissionRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemStepRepository problemStepRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ProblemContentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProblemDto> search(ProblemSearchQuery query, String username) {
        User me = requireUser(username);

        List<Problem> found = problemRepository.findAll(
                buildSpecification(query, me.getId()),
                Sort.by(Sort.Direction.DESC, "updatedAt"));

        Map<Long, Long> usage = usageCounts(found.stream().map(Problem::getId).toList());

        return found.stream()
                .map(problem -> mapper.toDto(
                        problem,
                        usage.getOrDefault(problem.getId(), 0L),
                        isOwnedBy(problem, me),
                        false)) // в списке правильных ответов нет никогда
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProblemDto get(Long problemId, String username) {
        User me = requireUser(username);
        Problem problem = findVisible(problemId, me);
        boolean owned = isOwnedBy(problem, me);

        return mapper.toDto(problem, problemStepRepository.countByProblemId(problemId), owned, owned);
    }

    @Override
    @Transactional
    public ProblemDto create(ProblemRequest request, String username) {
        Problem saved = createEntity(request, requireUser(username));
        return mapper.toDto(saved, 0L, true, true);
    }

    @Override
    @Transactional
    public Problem createEntity(ProblemRequest request, User author) {
        Problem problem = ProblemType.parse(request.getProblemType()).newInstance();
        mapper.apply(problem, request);
        problem.setAuthor(author);
        return problemRepository.save(problem);
    }

    @Override
    @Transactional
    public ProblemDto update(Long problemId, ProblemRequest request, String username) {
        Problem problem = requireOwned(problemId, username);

        // Тип задачи определяет таблицу подтипа — сменить его правкой нельзя.
        ProblemType requested = ProblemType.parse(request.getProblemType());
        if (requested != ProblemType.of(problem)) {
            throw AppException.badRequest(
                    "Нельзя изменить тип задачи — создайте новую задачу нужного типа");
        }

        mapper.apply(problem, request);
        Problem saved = problemRepository.save(problem);

        return mapper.toDto(saved, problemStepRepository.countByProblemId(problemId), true, true);
    }

    @Override
    @Transactional
    public void delete(Long problemId, String username) {
        Problem problem = requireOwned(problemId, username);

        // Задача из банка переживает урок, а не наоборот: пока она где-то стоит
        // или по ней есть решения, удалять её — значит рвать чужой курс и
        // историю студента. Оба случая объясняем словами, а не 500 от базы.
        long usages = problemStepRepository.countByProblemId(problemId);
        if (usages > 0) {
            throw AppException.conflict(
                    "Задача используется в уроках (" + usages + "). Сначала снимите её с уроков.");
        }
        if (submissionRepository.existsByProblemId(problemId)) {
            throw AppException.conflict("По задаче уже есть решения студентов — удалить её нельзя");
        }

        problemRepository.delete(problem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> tags(String username) {
        return problemRepository.findTagsByAuthorId(requireUser(username).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Problem requirePlaceable(Long problemId, String username) {
        return findVisible(problemId, requireUser(username));
    }

    @Override
    @Transactional(readOnly = true)
    public Problem requireOwned(Long problemId, String username) {
        User me = requireUser(username);
        Problem problem = findVisible(problemId, me);
        if (!isOwnedBy(problem, me)) {
            throw AppException.forbidden("Задача принадлежит другому преподавателю");
        }
        return problem;
    }

    // --- Внутреннее -------------------------------------------------------

    private Specification<Problem> buildSpecification(ProblemSearchQuery query, Long myId) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Predicate mine = cb.equal(root.get("author").get("id"), myId);
            predicates.add(query.onlyMine()
                    ? mine
                    : cb.or(mine, cb.equal(root.get("visibility"), ProblemVisibility.PUBLIC)));

            if (hasText(query.type())) {
                predicates.add(cb.equal(root.type(), ProblemType.parse(query.type()).entityClass()));
            }
            if (hasText(query.difficulty())) {
                predicates.add(cb.equal(root.get("difficulty"), parseDifficulty(query.difficulty())));
            }
            if (hasText(query.tag())) {
                predicates.add(cb.isMember(query.tag().trim().toLowerCase(), root.<List<String>>get("tags")));
            }
            if (hasText(query.q())) {
                String pattern = "%" + query.q().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<Long, Long> usageCounts(List<Long> problemIds) {
        if (problemIds.isEmpty()) {
            return Map.of();
        }
        return problemStepRepository.countUsages(problemIds).stream()
                .collect(Collectors.toMap(
                        ProblemStepRepository.ProblemUsage::getProblemId,
                        ProblemStepRepository.ProblemUsage::getUsageCount));
    }

    /**
     * Чужая личная задача неотличима от несуществующей — то же соглашение, что
     * и у чужого черновика курса в CourseAccessService.
     */
    private Problem findVisible(Long problemId, User me) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> AppException.notFound("Задача не найдена"));

        if (isOwnedBy(problem, me) || problem.getVisibility() == ProblemVisibility.PUBLIC) {
            return problem;
        }
        throw AppException.notFound("Задача не найдена");
    }

    private boolean isOwnedBy(Problem problem, User user) {
        return problem.getAuthor().getId().equals(user.getId());
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.unauthorized("Требуется вход в систему"));
    }

    private Difficulty parseDifficulty(String raw) {
        try {
            return Difficulty.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Неизвестная сложность: " + raw);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
