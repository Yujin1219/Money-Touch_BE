package com.server.money_touch.domain.home.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingSchedulerService {

    private final HomeService homeService;

    /**
     * 매주 월요일 0시에 지난 주 소비 기록 기반 랭킹 갱신
     */
    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul")
    public void updateWeeklyRanking() {
        log.info("🕛 [스케줄러] Weekly Wise Ranking 계산 시작");
        try {
            homeService.calculateAndSaveWeeklyWiseRanking();
            log.info("✅ [스케줄러] Weekly Wise Ranking 계산 완료");
        } catch (Exception e) {
            log.error("❌ [스케줄러] Weekly Wise Ranking 계산 실패: {}", e.getMessage(), e);
        }
    }

}
