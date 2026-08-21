package com.algoschool.step.repository;

import com.algoschool.step.entity.ProblemStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProblemStepRepository extends JpaRepository<ProblemStep, Long> {

    /** Сколько уроков используют задачу: правка задачи затрагивает их все. */
    long countByProblemId(Long problemId);

    /** То же самое для списка задач разом — иначе список банка делал бы запрос на строку. */
    @Query("SELECT ps.problem.id AS problemId, COUNT(ps) AS usageCount FROM ProblemStep ps "
            + "WHERE ps.problem.id IN :problemIds GROUP BY ps.problem.id")
    List<ProblemUsage> countUsages(@Param("problemIds") Collection<Long> problemIds);

    /**
     * Размещения задачи в курсах, на которые студент записан.
     * <p>
     * Нужно, чтобы решённая задача отмечалась пройденной во всех уроках, где
     * она стоит: до разделения задачи и размещения такой ситуации не было —
     * задача жила ровно в одном уроке, — а теперь без этого студент видел бы
     * непройденный шаг с уже засчитанным решением.
     */
    @Query("SELECT ps FROM ProblemStep ps WHERE ps.problem.id = :problemId "
            + "AND ps.lesson.module.course.id IN "
            + "(SELECT uc.course.id FROM UserCourse uc WHERE uc.user.id = :userId)")
    List<ProblemStep> findPlacementsInEnrolledCourses(@Param("problemId") Long problemId,
                                                      @Param("userId") Long userId);

    interface ProblemUsage {
        Long getProblemId();
        long getUsageCount();
    }
}
