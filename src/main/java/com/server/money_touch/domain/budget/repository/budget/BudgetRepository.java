package com.server.money_touch.domain.budget.repository.budget;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    Optional<Budget> findByUserIdAndCreatedMonth(Long userId, String createdMonth);
}
