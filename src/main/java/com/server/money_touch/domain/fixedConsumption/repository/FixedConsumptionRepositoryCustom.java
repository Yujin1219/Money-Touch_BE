package com.server.money_touch.domain.fixedConsumption.repository;

import com.server.money_touch.domain.fixedConsumption.entity.FixedConsumption;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface FixedConsumptionRepositoryCustom {
    // 커서 기반 고정비 목록 조회
    Slice<FixedConsumption> findFixedConsumptionsByCursor(Long userId, Long cursorId, Pageable pageable);
}
