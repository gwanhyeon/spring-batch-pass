package com.kgh.spring_batch_pass.job.statistics;


import com.kgh.spring_batch_pass.repository.statistics.AggregatedStatistics;
import com.kgh.spring_batch_pass.repository.statistics.StatisticsRepository;
import com.kgh.spring_batch_pass.util.CustomCSVWriter;
import com.kgh.spring_batch_pass.util.LocalDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@StepScope
public class MakeDailyStatisticsTasklet implements Tasklet {
    @Value("#{jobParameters[from]}")
    private String fromString;
    @Value("#{jobParameters[to]}")
    private String toString;
    private final StatisticsRepository statisticsRepository;

    public MakeDailyStatisticsTasklet(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // fromString과 toString이 null이면 기본값 설정
        final LocalDateTime from = Optional.ofNullable(fromString)
                .filter(str -> !str.isEmpty())
                .map(LocalDateTimeUtils::parse)
                .orElse(LocalDateTime.now()); // 기본값: 현재 시간

        final LocalDateTime to = Optional.ofNullable(toString)
                .filter(str -> !str.isEmpty())
                .map(LocalDateTimeUtils::parse)
                .orElse(LocalDateTime.now()); // 기본값: 현재 시간

        final List<AggregatedStatistics> statisticsList = statisticsRepository.findByStatisticsAtBetweenAndGroupBy(from, to);

        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"statisticsAt", "allCount", "attendedCount", "cancelledCount"});

        String fileDate = LocalDateTimeUtils.format(from, LocalDateTimeUtils.YYYY_MM_DD);

        for (AggregatedStatistics statistics : statisticsList) {
            // statistics.getStatisticsAt()이 null인지 체크 후 처리
            String formattedDate = Optional.ofNullable(statistics.getStatisticsAt())
                    .map(date -> LocalDateTimeUtils.format(date, LocalDateTimeUtils.YYYY_MM_DD))
                    .orElse("N/A");

            data.add(new String[]{
                    formattedDate,
                    String.valueOf(statistics.getAllCount()),
                    String.valueOf(statistics.getAttendedCount()),
                    String.valueOf(statistics.getCancelledCount())
            });
        }

        // CSV 파일 생성
        CustomCSVWriter.write("daily_statistics_" + fileDate + ".csv", data);

        return RepeatStatus.FINISHED;
    }
}
