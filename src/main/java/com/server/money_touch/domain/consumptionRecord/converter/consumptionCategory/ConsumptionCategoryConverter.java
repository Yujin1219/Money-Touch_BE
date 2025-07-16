package com.server.money_touch.domain.consumptionRecord.converter.consumptionCategory;

import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.user.entity.User;

public class ConsumptionCategoryConverter {

    // 카테고리 이름과 타입 → 소비 카테고리 엔티티
    public static ConsumptionCategory toConsumptionCategory(User user, String name, CategoryType type) {
        return ConsumptionCategory.builder()
                .user(user)
                .budgetCategoryName(name)
                .budgetCategoryType(type)
                .build();
    }
}
