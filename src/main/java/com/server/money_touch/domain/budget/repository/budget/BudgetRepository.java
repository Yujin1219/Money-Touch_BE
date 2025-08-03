package com.server.money_touch.domain.budget.repository.budget;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    Optional<Budget> findByUserIdAndCreatedMonth(Long userId, String createdMonth);

    Optional<Budget> findByUserAndCreatedMonth(User user, String createdMonth);

    @Query(value = """
    SELECT * FROM budget b
    WHERE b.user_id = :userId
    AND b.created_at BETWEEN :start AND :end
    AND TIMESTAMPDIFF(SECOND, b.created_at, b.updated_at) != 0
    AND b.budget_total > 0
    LIMIT 1
    """, nativeQuery = true)
    Optional<Budget> findRegisteredBudgetInMonthNative(@Param("userId") Long userId,
                                                       @Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);
}
