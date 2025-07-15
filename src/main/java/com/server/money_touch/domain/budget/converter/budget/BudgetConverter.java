package com.server.money_touch.domain.budget.converter.budget;

import com.server.money_touch.domain.budget.dto.BudgetRequest;
import com.server.money_touch.domain.budget.dto.BudgetResponse;
import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.user.entity.User;

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
}
