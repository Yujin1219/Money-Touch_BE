package com.server.money_touch.domain.budget.repository.budgetCategory;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.enums.CategoryType;
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
}

