package com.algoschool.grade.service;

import com.algoschool.grade.dto.*;
import com.algoschool.problem.entity.Problem;
import com.algoschool.step.entity.ProblemStep;
import com.algoschool.user.entity.User;

import java.util.List;

public interface GradeService {

    // --- Сопровождение элементов журнала ----------------------------------

    /** Заводит столбец журнала для задачи, поставленной в урок. */
    void onProblemStepCreated(ProblemStep step);

    /** Снимает столбец журнала вместе с задачей, снятой с урока. */
    void onProblemStepRemoved(Long stepId);

    // --- Пересчёт ----------------------------------------------------------

    /** После отправки решения или прихода вердикта. */
    void recomputeForProblem(User user, Problem problem);

    /** При зачислении на курс: студент мог решить эти задачи раньше, в другом курсе. */
    void recomputeCourseForUser(Long courseId, Long userId);

    // --- Журнал преподавателя ---------------------------------------------

    GradebookDto gradebook(Long courseId, String teacherUsername);

    /** Тот же журнал в CSV (UC-T-45). */
    String exportGradebookCsv(Long courseId, String teacherUsername);

    List<GradeItemDto> items(Long courseId, String teacherUsername);

    GradeItemDto createManualItem(Long courseId, GradeItemRequest request, String teacherUsername);

    GradeItemDto updateItem(Long itemId, GradeItemRequest request, String teacherUsername);

    /** Удалить можно только ручной элемент: столбец задачи снимается вместе с шагом. */
    void deleteItem(Long itemId, String teacherUsername);

    void setManualGrade(GradeUpdateRequest request, String teacherUsername);

    /** Снимает ручную оценку и возвращает клетку под автоматический пересчёт. */
    void clearManualGrade(Long gradeItemId, Long userId, String teacherUsername);

    // --- Оценки студента ---------------------------------------------------

    MyGradesDto myGrades(Long courseId, String username);
}
