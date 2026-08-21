package com.algoschool.problem.service;

import com.algoschool.problem.dto.ProblemDto;
import com.algoschool.problem.dto.ProblemRequest;
import com.algoschool.problem.dto.ProblemSearchQuery;
import com.algoschool.problem.entity.Problem;
import com.algoschool.user.entity.User;

import java.util.List;

public interface ProblemService {

    /** Свои задачи плюс чужие публичные. Правильные ответы в выдачу не попадают. */
    List<ProblemDto> search(ProblemSearchQuery query, String username);

    /** Карточка задачи. Правильные ответы видит только автор. */
    ProblemDto get(Long problemId, String username);

    ProblemDto create(ProblemRequest request, String username);

    ProblemDto update(Long problemId, ProblemRequest request, String username);

    /** Удаление возможно, только пока задача ни в одном уроке и по ней нет решений. */
    void delete(Long problemId, String username);

    /** Теги автора — для подсказок в фильтре. */
    List<String> tags(String username);

    // --- Для размещения задачи в уроке ------------------------------------

    /**
     * Задача, которую этому преподавателю позволено поставить в свой урок:
     * своя любая, чужая — только публичная.
     */
    Problem requirePlaceable(Long problemId, String username);

    /** Задача, которую этому преподавателю позволено править. */
    Problem requireOwned(Long problemId, String username);

    /** Заводит задачу в банке; используется и редактором урока при создании шага. */
    Problem createEntity(ProblemRequest request, User author);
}
