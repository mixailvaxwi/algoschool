package com.algoschool.step.service;

import com.algoschool.exception.AppException;

import com.algoschool.step.entity.Step;
import com.algoschool.step.dto.StepCreateRequest;
import com.algoschool.step.entity.*;
import com.algoschool.step.repository.StepRepository;
import com.algoschool.course.entity.Lesson;
import com.algoschool.course.repository.LessonRepository;
import com.algoschool.course.service.CourseAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StepServiceImpl implements StepService {
    private final LessonRepository lessonRepository;
    private final StepRepository stepRepository;
    private final CourseAccessService courseAccess;

    @Override
    @Transactional
    public Step addStepToLesson(Long lessonId, StepCreateRequest request, String username) {
        // Шаг можно добавить только в свой курс. Раньше проверки не было вовсе:
        // достаточно было знать lessonId, чтобы дописать шаг в чужой урок.
        courseAccess.requireAuthorOfLesson(lessonId, username);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> AppException.notFound("Урок не найден: " + lessonId));

        Step step;
        switch (request.getStepType()) {
            case "THEORY":
                TheoryStep theory = new TheoryStep();
                theory.setContent(request.getContent());
                step = theory;
                break;
            case "INPUT_PROBLEM":
                TextProblem input = new TextProblem();
                input.setDescription(request.getDescription());
                input.setCorrectAnswer(request.getCorrectAnswer());
                step = input;
                break;
            case "CHOICE_PROBLEM":
                ChoiceProblem choice = new ChoiceProblem();
                choice.setDescription(request.getDescription());
                choice.setOptions(request.getOptions());
                choice.setCorrectOptionIndex(request.getCorrectOptionIndex());
                choice.setMultipleChoice(request.getIsMultipleChoice() != null ? request.getIsMultipleChoice() : false);
                step = choice;
                break;
            case "CODE_PROBLEM":
                CodeProblem code = new CodeProblem();
                code.setDescription(request.getDescription());
                code.setTimeLimit(request.getTimeLimitSec());
                code.setMemoryLimit(request.getMemoryLimitMb());
                code.setAllowedLanguages(request.getAllowedLanguages());
                code.setEjudgeContestId(request.getEjudgeContestId());
                code.setEjudgeProblemId(request.getEjudgeProblemId());
                step = code;
                break;
            default:
                throw AppException.badRequest("Неизвестный тип шага: " + request.getStepType());
        }

        // ВАЖНО: устанавливаем обязательные поля
        step.setLesson(lesson);
        step.setOrderIndex(request.getOrderIndex());

        return stepRepository.save(step);
    }
}