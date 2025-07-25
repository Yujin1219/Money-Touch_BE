package com.server.money_touch.domain.fixedConsumption.service;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.budget.service.budget.BudgetCommandService;
import com.server.money_touch.domain.consumptionRecord.converter.consumptionRecord.ConsumptionRecordConverter;
import com.server.money_touch.domain.consumptionRecord.converter.totalConsumption.TotalConsumptionConverter;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.TotalConsumption;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.consumptionRecord.repository.totalConsumption.TotalConsumptionRepository;
import com.server.money_touch.domain.fixedConsumption.repository.FixedConsumptionRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixedConsumptionSchedulerService {

    private final UserRepository userRepository;
    private final FixedConsumptionRepository fixedConsumptionRepository;
    private final ConsumptionCategoryRepository consumptionCategoryRepository;
    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final TotalConsumptionRepository totalConsumptionRepository;
    private final BudgetCommandService budgetCommandService;

    /**
     * 매월 1일 오전 12시 실행되는 스케줄러 - 고정비를 소비 기록으로 등록
     */
    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul") // 매월 1일 자정(00:00)
    public void registerFixedConsumptionsToRecords() {
        log.info("🕛 [스케줄러] 고정비 소비 기록 등록 작업 시작");

        List<User> users = userRepository.findAllWithUserDetail();

        users.forEach(this::processUserFixedConsumption);

        log.info("🕔 [스케줄러] 고정비 소비 기록 등록 전체 비동기 작업 호출 완료");
    }

    /**
     * 사용자 1명에 대한 고정비 소비 기록 등록 (비동기)
     */
    @Async("customAsyncExecutor")
    @Transactional
    public void processUserFixedConsumption(User user) {
        try {
            LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

            // 1. Budget, 기본 ConsumptionCategory 테이블 조회 or 생성
            Budget budget = budgetCommandService.createOrFindBudgetForMonth(user);
            budgetCommandService.saveCategoryBudgetsByType(null, user, budget, CategoryType.DEFAULT);

            // 2. TotalConsumption 조회 or 생성
            TotalConsumption totalConsumption = totalConsumptionRepository
                    .findByUserAndCreatedAtBetween(user, startOfMonth, endOfMonth)
                    .orElseGet(() -> totalConsumptionRepository.save(
                            TotalConsumptionConverter.toTotalConsumption(user))
                    );

            // 3. 기본 ConsumptionCategory 목록 불러오기 (카테고리 이름 기준 매핑)
            Map<String, ConsumptionCategory> categoryMap = consumptionCategoryRepository
                    .findAllByUserAndBudgetCategoryType(user, CategoryType.DEFAULT)
                    .stream()
                    .collect(Collectors.toMap(ConsumptionCategory::getBudgetCategoryName, c -> c));

            // 4~6. 고정비 목록을 기반으로 소비 기록 생성 및 저장 + TotalConsumption 반영
            fixedConsumptionRepository.findAllByUser(user).stream()
                    .map(fc -> {
                        String categoryName = fc.getCategoryName();
                        ConsumptionCategory category = categoryMap.get(categoryName);
                        if (category == null) {
                            log.warn("❌ 고정비 카테고리 매핑 실패 - userId: {}, categoryName: {}", user.getId(), categoryName);
                            return null;
                        }

                        // 소비 기록 생성
                        ConsumptionRecord record = ConsumptionRecordConverter.toConsumptionRecordForFix(user, category, fc, startOfMonth);

                        // TotalConsumption 금액 반영
                        totalConsumption.updateAddTotalConsumptionAmount(fc.getFixedConsumptionAmount());
                        totalConsumptionRepository.save(totalConsumption); // 명시적 저장

                        return record;
                    })
                    .filter(Objects::nonNull)
                    .forEach(consumptionRecordRepository::save);

            log.info("✅ [스케줄러] 사용자 {} 고정비 소비 기록 등록 완료", user.getId());

        } catch (Exception e) {
            log.error("❌ [스케줄러] 사용자 {} 고정비 소비 기록 등록 실패: {}", user.getId(), e.getMessage(), e);
        }
    }
}