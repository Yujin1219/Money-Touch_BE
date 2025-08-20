package com.server.money_touch.domain.fixedConsumption.service;

import com.server.money_touch.domain.fixedConsumption.dto.FixedConsumptionRequest;
import com.server.money_touch.domain.fixedConsumption.dto.FixedConsumptionResponse;
import com.server.money_touch.global.validation.annotation.ExistUser;

public interface FixedConsumptionCommandService {
    // 고정비 등록
    FixedConsumptionResponse.FixedConsumptionCreateResultDTO saveFixedConsumption(@ExistUser Long userId, FixedConsumptionRequest.FixedConsumptionCreateDTO request);

    // 고정비 수정
    void updateFixedConsumption(
            @ExistUser Long userId, Long fixedConsumptionId, FixedConsumptionRequest.FixedConsumptionCreateDTO request);

    // 고정비 삭제
    void deleteFixedConsumption(@ExistUser Long userId, Long fixedConsumptionId);

    // 고정비 수동 갱신
    void postFixedConsumptionsByManual();
}
