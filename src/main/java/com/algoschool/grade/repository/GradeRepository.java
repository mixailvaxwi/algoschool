package com.algoschool.grade.repository;

import com.algoschool.grade.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findByGradeItemIdAndUserId(Long gradeItemId, Long userId);

    /** Все оценки курса разом: журнал не должен делать запрос на клетку. */
    @Query("SELECT g FROM Grade g WHERE g.gradeItem.course.id = :courseId")
    List<Grade> findAllByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT g FROM Grade g WHERE g.gradeItem.course.id = :courseId AND g.user.id = :userId")
    List<Grade> findAllByCourseIdAndUserId(@Param("courseId") Long courseId, @Param("userId") Long userId);

    /** Оценки снимаются вместе с элементом — например, когда задачу сняли с урока. */
    @Modifying
    @Query("DELETE FROM Grade g WHERE g.gradeItem.id = :gradeItemId")
    void deleteByGradeItemId(@Param("gradeItemId") Long gradeItemId);
}
