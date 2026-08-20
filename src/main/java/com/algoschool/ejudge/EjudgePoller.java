package com.algoschool.ejudge;

import com.algoschool.ejudge.dto.EjudgeRunStatusResponse;
import com.algoschool.submission.dto.PendingRun;
import com.algoschool.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Опрос вердиктов у Ejudge.
 * <p>
 * Вынесен из SubmissionService по двум причинам.
 * <p>
 * Первая: раньше метод был помечен и {@code @Scheduled}, и {@code @Transactional},
 * а внутри цикла делал HTTP-запросы. Транзакция висела открытой всё время
 * общения по сети — при недоступном Ejudge (и без таймаутов) неограниченно долго.
 * Здесь сеть живёт вне транзакции, а короткие транзакции открывает
 * SubmissionService — и, поскольку это отдельный бин, вызовы идут через прокси,
 * а не мимо него, как было бы при вызове самого себя.
 * <p>
 * Вторая: опрос выключается одним свойством. Без Ejudge он раньше писал ошибку
 * в лог каждые три секунды.
 * <p>
 * Опрос защищён {@code @SchedulerLock} (см. ShedLockConfig): если поднято
 * несколько инстансов приложения, круг опроса выполнит только тот, кто
 * первым взял блокировку в таблице {@code shedlock}, а не каждый инстанс сразу.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "ejudge.polling.enabled", havingValue = "true", matchIfMissing = true)
public class EjudgePoller {

    private final SubmissionService submissionService;
    private final EjudgeClient ejudgeClient;

    @Scheduled(fixedDelayString = "${ejudge.polling.interval-ms:3000}")
    @SchedulerLock(name = "ejudge-poll-pending-runs", lockAtMostFor = "PT2M", lockAtLeastFor = "PT2S")
    public void pollPendingRuns() {
        List<PendingRun> pending = submissionService.findPendingRuns();
        if (pending.isEmpty()) {
            return;
        }

        for (PendingRun run : pending) {
            // Сеть — вне транзакции
            EjudgeRunStatusResponse response =
                    ejudgeClient.getRunStatus(run.contestId(), run.externalRunId());

            if (response == null || !response.isOk() || response.getResult() == null) {
                continue; // недоступен или ответил не тем — спросим на следующем круге
            }

            // Запись — в короткой отдельной транзакции
            submissionService.applyEjudgeVerdict(run.submissionId(), response);
        }
    }
}
