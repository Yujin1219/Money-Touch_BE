package com.server.money_touch.domain.routine.repository.routine;

import com.server.money_touch.domain.routine.entity.Routine;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long>, RoutineRepositoryCustom {

    // 현재 예산(budgetId)과 연월(createdMonth)에 해당하는 루틴이 존재하는지 확인 + PESSIMISTIC_WRITE 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Routine r " +
            "WHERE r.user.id = :userId " +
            "AND r.budget.id = :budgetId " +
            "AND r.createdMonth = :createdMonth")
    Optional<Routine> findForUpdateByUserAndBudgetAndMonth(
            @Param("userId") Long userId,
            @Param("budgetId") Long budgetId,
            @Param("createdMonth") String createdMonth
    );

    Optional<Routine> findByIdAndUserId(Long routineId, Long userId);
}
