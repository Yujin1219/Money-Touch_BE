package com.server.money_touch.domain.budget.converter.budget;

import com.server.money_touch.domain.budget.dto.BudgetRequest;
import com.server.money_touch.domain.budget.dto.BudgetResponse;
import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.user.entity.User;

import java.util.List;

public class BudgetConverter {

    // BudgetCreateDTO → Budget Entity 변환
    public static Budget toBudgetEntity(User user, BudgetRequest.BudgetCreateDTO request) {
        return Budget.builder()
                .user(user)
                .budgetTotal(request.getTotalBudget())
                .build();
    }

    // Budget Entity → BudgetCreateResultDTO 변환
    public static BudgetResponse.BudgetCreateResultDTO toBudgetCreateResultDto(Long budgetId) {
        return BudgetResponse.BudgetCreateResultDTO.builder()
                .budgetId(budgetId)
                .build();
    }

    // 내 예산 조회 응답 DTO 반환
    public static BudgetResponse.BudgetDetailDTO toBudgetDetailDTO(Budget budget,
                                                                   List<BudgetResponse.DefaultCategoryBudgetResponse> defaultCategories,
                                                                   List<BudgetResponse.CustomCategoryBudgetResponse> customCategories,
                                                                   List<BudgetResponse.RoutineCategoryBudgetResponse> routineCategories) {
        return BudgetResponse.BudgetDetailDTO.builder()
                .totalBudget(budget.getBudgetTotal())
                .defaultCategoryBudgets(defaultCategories)
                .customCategoryBudgets(customCategories)
                .routineCategoryBudgets(routineCategories)
                .build();
    }

    // 예산 아이디 및 총 소비 금액 조회 응답 DTO 반환
    public static BudgetResponse.TotalConsumptionResultDTO toTotalConsumptionResultDto(Long budgetId, Integer totalConsumptionAmount) {
        return BudgetResponse.TotalConsumptionResultDTO.builder()
                .budgetId(budgetId)
                .totalConsumptionAmount(totalConsumptionAmount)
                .build();
    }
}
