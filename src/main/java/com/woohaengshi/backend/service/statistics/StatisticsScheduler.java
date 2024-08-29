package com.woohaengshi.backend.service.statistics;

import com.woohaengshi.backend.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatisticsScheduler {
    private final StatisticsRepository statisticsRepository;
    @Scheduled(cron = "0 0 5 1 * ?", zone = "Asia/Seoul")
    private void initMonthlyStatistics() {
        statisticsRepository.initWeeklyTime();
    }

    @Scheduled(cron = "0 0 5 * * MON", zone = "Asia/Seoul")
    private void initWeeklyStatistics() {
        statisticsRepository.initMonthlyTime();
    }
}
