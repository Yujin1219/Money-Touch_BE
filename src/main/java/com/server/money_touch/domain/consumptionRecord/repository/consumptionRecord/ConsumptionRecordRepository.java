package com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord;

import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsumptionRecordRepository extends JpaRepository<ConsumptionRecord, Long>, ConsumptionRecordRepositoryCustom {

    List<ConsumptionRecord> findAllByIsPublicTrueAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 이번 달 소비기록 그룹으로 묶고, 금액 합산
    @Query("""
    SELECT cr.consumptionCategory.budgetCategoryName, SUM(cr.amount)
    FROM ConsumptionRecord cr
    WHERE cr.user = :user AND cr.consumeDate BETWEEN :start AND :end
    GROUP BY cr.consumptionCategory.budgetCategoryName """)
    List<Object[]> findCategorySpendingBetween(User user, LocalDateTime start, LocalDateTime end);

}