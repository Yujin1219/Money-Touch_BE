package com.server.money_touch.domain.fixedConsumption.service;

import com.server.money_touch.domain.fixedConsumption.repository.FixedConsumptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FixedConsumptionQueryServiceImpl implements FixedConsumptionQueryService {

    private final FixedConsumptionRepository fixedConsumptionRepository;

    // 고정비 존재 여부 검증
    @Override
    public Boolean existsFixedConsumptionById(Long fixedConsumptionId) {
        return fixedConsumptionRepository.findById(fixedConsumptionId).isPresent();
    }
}
