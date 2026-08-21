package com.algoschool.grade.repository;

import com.algoschool.grade.entity.GradeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeItemRepository extends JpaRepository<GradeItem, Long> {

    List<GradeItem> findAllByCourseIdOrderByOrderIndexAsc(Long courseId);

    Optional<GradeItem> findByStepId(Long stepId);

    @Query("SELECT COALESCE(MAX(i.orderIndex), 0) FROM GradeItem i WHERE i.course.id = :courseId")
    int findMaxOrderIndex(@Param("courseId") Long courseId);

    /**
     * Элементы, которые задевает решение задачи: она может стоять в нескольких
     * уроках, и засчитывается везде, где стоит, — так же, как отметка о
     * прохождении. Курсы, на которые студент не записан, отсекаются здесь же:
     * журнал курса не должен показывать баллы постороннего человека.
     */
    @Query("SELECT i FROM GradeItem i WHERE i.step.problem.id = :problemId "
            + "AND i.course.id IN (SELECT uc.course.id FROM UserCourse uc WHERE uc.user.id = :userId)")
    List<GradeItem> findByProblemForEnrolledUser(@Param("problemId") Long problemId,
                                                 @Param("userId") Long userId);
}
