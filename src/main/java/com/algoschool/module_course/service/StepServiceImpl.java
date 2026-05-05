package com.algoschool.module_course.service;

import com.algoschool.module_assessment.entity.*;
import com.algoschool.module_assessment.repository.StepRepository;
import com.algoschool.module_course.dto.*;
import com.algoschool.module_course.entity.Lesson;
import com.algoschool.module_course.repository.LessonRepository;
import com.algoschool.module_course.service.StepService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
// ... ваши импорты DTO и Entity

@Service
@RequiredArgsConstructor
public class StepServiceImpl implements StepService {

    private final LessonRepository lessonRepository;
    private final StepRepository stepRepository;

    @Override
    @Transactional
    public void addStepToLesson(Long lessonId, TeacherStepRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Урок с ID " + lessonId + " не найден"));

        Step step;

        // Определяем тип шага и создаем нужную сущность
        if (request instanceof TeacherTheoryStepRequest theoryReq) {
            Theory theory = new Theory();
            theory.setContent(theoryReq.getContent());
            step = theory;

        } else if (request instanceof TeacherCodeProblemRequest codeReq) {
            CodeProblem code = new CodeProblem();
            code.setDescription(codeReq.getDescription());
            code.setTimeLimitSec(codeReq.getTimeLimitSec());
            code.setMemoryLimitMb(codeReq.getMemoryLimitMb());
            code.setAllowedLanguages(codeReq.getAllowedLanguages());
            step = code;

        } else if (request instanceof TeacherInputProblemRequest inputReq) {
            TextProblem textProb = new TextProblem();
            textProb.setCorrectAnswer(inputReq.getCorrectAnswer());
            textProb.setMatchType("EXACT");
            step = textProb;

        } else if (request instanceof TeacherChoiceProblemRequest choiceReq) {
            ChoiceProblem choice = new ChoiceProblem();
            choice.setCorrectOptionIndex(choiceReq.getCorrectOptionIndex());
            choice.setOptions(choiceReq.getOptions());
            step = choice;

        } else {
            throw new IllegalArgumentException("Неизвестный тип шага");
        }

        // Общие поля для всех шагов
        step.setLesson(lesson);
        step.setPositionIndex(request.getOrderIndex());

        // Сохраняем (Hibernate сам поймет, в какую дочернюю таблицу сделать INSERT)
        stepRepository.save(step);
    }
}