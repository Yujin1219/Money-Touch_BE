package com.server.money_touch.domain.budget.repository.budgetCategory;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long>, BudgetCategoryRepositoryCustom {
    @Query("""
    SELECT bc FROM BudgetCategory bc
    JOIN FETCH bc.consumptionCategory cc
    WHERE bc.budget = :budget
""")
    List<BudgetCategory> findAllWithCategoryByBudget(@Param("budget") Budget budget);

    @Query("""
        select bc 
        from BudgetCategory bc
        join fetch bc.consumptionCategory cc
        where bc.budget = :budget
          and cc.budgetCategoryType = :type
    """)
    List<BudgetCategory> findAllWithCategoryByBudgetAndType(@Param("budget") Budget budget,
                                                            @Param("type") CategoryType type);

    // ConsumptionCategory를 참조하는 BudgetCategory의 개수를 반환
    long countByConsumptionCategory(ConsumptionCategory category);

    // budgetId 기준으로 BudgetCategory를 조회하면서 연관된 ConsumptionCategory도 JOIN FETCH로 한 번에 가져오도록
    @Query("SELECT bc FROM BudgetCategory bc JOIN FETCH bc.consumptionCategory WHERE bc.budget.id = :budgetId")
    List<BudgetCategory> findAllByBudgetIdWithCategory(@Param("budgetId") Long budgetId);

    List<BudgetCategory> findByBudget(Budget budget);

    @Query("SELECT bc FROM BudgetCategory bc JOIN FETCH bc.consumptionCategory WHERE bc.budget = :budget")
    List<BudgetCategory> findByBudgetWithConsumptionCategory(@Param("budget") Budget budget);
}

