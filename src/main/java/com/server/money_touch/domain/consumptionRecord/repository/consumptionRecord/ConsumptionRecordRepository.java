package com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord;

import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsumptionRecordRepository extends JpaRepository<ConsumptionRecord, Long>, ConsumptionRecordRepositoryCustom {

    List<ConsumptionRecord> findAllByIsPublicTrueAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}