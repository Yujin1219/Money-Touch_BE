package com.server.money_touch.domain.budget.converter.budget_category;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;

public class BudgetCategoryConverter {

    // 소비 카테고리 + 예산 정보 → 카테고리별 예산 엔티티
    public static BudgetCategory toBudgetCategory(Budget budget, ConsumptionCategory category, Integer amount) {
        return BudgetCategory.builder()
                .budget(budget)
                .consumptionCategory(category)
                .budgetCategoryMoney(amount)
                .build();
    }
}
