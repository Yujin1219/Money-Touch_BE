package com.server.money_touch.domain.budget.repository.budgetCategory;


import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.entity.QBudgetCategory;
import com.server.money_touch.domain.consumptionRecord.entity.QConsumptionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class BudgetCategoryRepositoryImpl implements BudgetCategoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    QBudgetCategory budgetCategory = QBudgetCategory.budgetCategory;
    QConsumptionCategory consumptionCategory = QConsumptionCategory.consumptionCategory;

    @Override
    public List<BudgetCategory> findAllWithCategoryByBudgetId(Long budgetId) {
        return queryFactory
                .selectFrom(budgetCategory)
                .join(budgetCategory.consumptionCategory, consumptionCategory).fetchJoin()
                .where(budgetCategory.budget.id.eq(budgetId))
                .fetch();
    }
}
