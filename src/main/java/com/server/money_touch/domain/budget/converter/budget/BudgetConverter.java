package com.server.money_touch.domain.budget.converter.budget;

import com.server.money_touch.domain.budget.dto.BudgetResponse;
import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.user.entity.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BudgetConverter {

    // BudgetCreateDTO → Budget Entity 변환
    public static Budget toBudgetEntity(User user, Integer budgetTotal, String createdMonth) {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return Budget.builder()
                .user(user)
                .budgetTotal(budgetTotal)
                .createdMonth(createdMonth)
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
                                                                   List<BudgetResponse.BudgetDetailCategoryBudgetResponse> defaultCategories,
                                                                   List<BudgetResponse.BudgetDetailCategoryBudgetResponse> customCategories,
                                                                   List<BudgetResponse.BudgetDetailCategoryBudgetResponse> routineCategories) {
        return BudgetResponse.BudgetDetailDTO.builder()
                .totalBudget(budget.getBudgetTotal())
                .defaultCategoryBudgets(defaultCategories)
                .customCategoryBudgets(customCategories)
                .routineCategoryBudgets(routineCategories)
                .build();
    }

    // 내 예산 조회 시 카테고리별 예산 응답 DTO 변환
    public static BudgetResponse.BudgetDetailCategoryBudgetResponse toBudgetDetailCategoryBudgetResponse(BudgetCategory bc) {
        return BudgetResponse.BudgetDetailCategoryBudgetResponse.builder()
                .categoryName(bc.getConsumptionCategory().getBudgetCategoryName())
                .amount(bc.getBudgetCategoryMoney())
                .categoryType(bc.getConsumptionCategory().getBudgetCategoryType())
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
