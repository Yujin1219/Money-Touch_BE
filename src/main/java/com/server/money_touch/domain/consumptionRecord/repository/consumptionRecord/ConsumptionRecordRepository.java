package com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord;

import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumptionRecordRepository extends JpaRepository<ConsumptionRecord, Long>, ConsumptionRecordRepositoryCustom {
}