package com.server.money_touch.domain.routine.repository.routine;

import com.server.money_touch.domain.routine.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long>, RoutineRepositoryCustom {
    // 이번달에 이미 등록된 소비 루틴이 있는지 검증
    @Query("SELECT COUNT(r) > 0 FROM Routine r WHERE r.user.id = :userId AND MONTH(r.createdAt) = MONTH(CURRENT_DATE) AND YEAR(r.createdAt) = YEAR(CURRENT_DATE)")
    boolean existsByUserIdInCurrentMonth(@Param("userId") Long userId);

    Optional<Routine> findByIdAndUserId(Long routineId, Long userId);
}
