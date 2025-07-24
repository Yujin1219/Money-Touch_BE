package com.server.money_touch.domain.fixedConsumption.service;

public interface FixedConsumptionQueryService {
    // 고정비 기록 존재 여부 검증
    Boolean existsFixedConsumptionById(Long fixedConsumptionId);
}
