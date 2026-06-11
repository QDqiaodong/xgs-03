package com.plantcare.scheduler;

import com.plantcare.service.HotnessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostHotnessScheduler {

    private final HotnessService hotnessService;

    @Scheduled(cron = "0 0 * * * ?")
    public void recalculateHotness() {
        log.info("Hourly hotness recalculation triggered.");
        try {
            hotnessService.recalculateAllHotness();
        } catch (Exception e) {
            log.error("Failed to recalculate hotness scores", e);
        }
    }
}
