package com.server.money_touch.domain.fixedConsumption.service;

import com.server.money_touch.domain.fixedConsumption.dto.FixedConsumptionResponse;
import com.server.money_touch.global.validation.annotation.ExistUser;

public interface FixedConsumptionQueryService {
    // 고정비 기록 존재 여부 검증
    Boolean existsFixedConsumptionById(Long fixedConsumptionId);

    // 고정비 목록 조회 (커서 기반 무한스크롤)
    FixedConsumptionResponse.FixedConsumptionCursorResultDTO getFixedConsumptions(@ExistUser Long userId, Long cursorId);
}
