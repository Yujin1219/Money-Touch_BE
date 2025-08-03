package com.server.money_touch.domain.routine.repository.routine;

import com.server.money_touch.domain.routine.entity.RoutineAmount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoutineAmountRepository extends JpaRepository<RoutineAmount, Long> {

    @Query("SELECT ra FROM RoutineAmount ra JOIN FETCH ra.routine WHERE ra.routine.id = :routineId")
    List<RoutineAmount> findAllWithRoutineByRoutineId(@Param("routineId") Long routineId);
}
