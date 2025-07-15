package com.server.money_touch.domain.consumptionRecord.repository.consumption_category;

import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsumptionCategoryRepository extends JpaRepository<ConsumptionCategory, Long> {
    Optional<ConsumptionCategory> findByUserAndBudgetCategoryNameAndBudgetCategoryType(User user, String budgetCategoryName, CategoryType type);

    List<ConsumptionCategory> findAllByUserAndBudgetCategoryType(User user, CategoryType type);
}