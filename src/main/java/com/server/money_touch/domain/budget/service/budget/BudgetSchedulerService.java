package com.server.money_touch.domain.budget.service.budget;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class BudgetSchedulerService {

    private final BudgetCommandService budgetCommandService;
    private final UserRepository userRepository;

    /**
     * 매년 1일 12에 전체 유저에 대해 기본 카테고리를 비동기로 등록
     */
    @Async("customAsyncExecutor")
    @Scheduled(cron = "0 0 12 1 * *", zone = "Asia/Seoul") // 매월 1일 12:00// 매월 1일 12:00
    public void registerMonthlyDefaultCategoryBudgets() {
        log.info("🕛 [스케줄러] Budget, 기본 ConsumptionCategory 테이블 생성 작업 시작");

        List<User> users = userRepository.getAllBy();
        users.forEach(user ->
                CompletableFuture.runAsync(() -> {
                    try {
                        Budget budget = budgetCommandService.createOrFindBudgetForMonth(user);
                        budgetCommandService.saveCategoryBudgetsByType(null, user, budget, CategoryType.DEFAULT);
                        log.info("✅ [스케줄러] 사용자 {}에 대한 기본 카테고리 등록 완료", user.getId());
                    } catch (Exception e) {
                        log.error("❌ [스케줄러] 사용자 {}에 대한 기본 카테고리 등록 실패: {}", user.getId(), e.getMessage(), e);
                    }
                })
        );

        log.info("🕔 [스케줄러] Budget, 기본 ConsumptionCategory 테이블 생성 전체 비동기 작업 등록 완료");
    }
}
