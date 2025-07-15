package com.server.money_touch.domain.budget.repository.budget_category;

import com.server.money_touch.domain.budget.entity.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {
}
