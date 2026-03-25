package com.example.stockdashboard.scheduler;

import com.example.stockdashboard.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyAnalysisScheduler {

    private final AnalysisService analysisService;

    @Scheduled(cron = "${app.scheduler.cron:0 5 1 * * *}")
    public void refreshDaily() {
        log.info("Running daily stock analysis job");
        analysisService.runFullAnalysis();
    }
}
