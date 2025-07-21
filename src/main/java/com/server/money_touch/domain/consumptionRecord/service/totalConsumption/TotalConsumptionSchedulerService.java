package com.server.money_touch.domain.consumptionRecord.service.totalConsumption;


import com.server.money_touch.domain.consumptionRecord.converter.totalConsumption.TotalConsumptionConverter;
import com.server.money_touch.domain.consumptionRecord.repository.totalConsumption.TotalConsumptionRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TotalConsumptionSchedulerService {

    private final UserRepository userRepository;
    private final TotalConsumptionRepository totalConsumptionRepository;

    /**
     * 매년 1일 12시에 전체 유저의 월별 총 소비 금액을 비동기로 생성
     */
    @Scheduled(cron = "0 0 12 1 * *", zone = "Asia/Seoul") // 매월 1일 12:00
    public void generateMonthlyTotalConsumption() {
        log.info("🕛 [스케줄러] TotalConsumption 총 소비 금액 테이블 생성 작업 시작");

        List<User> users = userRepository.getAllBy();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        users.forEach(user -> CompletableFuture.runAsync(() -> {
            try {
                boolean exists = totalConsumptionRepository
                        .findByUserAndCreatedAtBetween(user, startOfMonth, endOfMonth)
                        .isPresent();

                if (!exists) {
                    totalConsumptionRepository.save(TotalConsumptionConverter.toTotalConsumption(user));
                    log.info("✅ 사용자 {}의 총 소비 금액 생성 완료", user.getId());
                } else {
                    log.info("⏭ 사용자 {}의 총 소비 금액은 이미 존재함", user.getId());
                }
            } catch (Exception e) {
                log.error("❌ 사용자 {} 총 소비 금액 생성 실패: {}", user.getId(), e.getMessage(), e);
            }
        }));

        log.info("🕔 [스케줄러] TotalConsumption 총 소비 금액 테이블 생성 전체 비동기 작업 등록 완료");
    }
}
