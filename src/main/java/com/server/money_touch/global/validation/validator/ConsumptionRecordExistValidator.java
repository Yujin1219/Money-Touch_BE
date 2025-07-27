package com.server.money_touch.global.validation.validator;

import com.server.money_touch.domain.consumptionRecord.service.ConsumptionRecordQueryService;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ExistConsumptionRecord;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 검증 대상은 Long
@Slf4j
@RequiredArgsConstructor
@Component
public class ConsumptionRecordExistValidator implements ConstraintValidator<ExistConsumptionRecord,Long> {

    private final ConsumptionRecordQueryService consumptionRecordQueryService;

    @Override
    public void initialize(ExistConsumptionRecord constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        // 파라미터로 넘어온 소비 기록 아이디가 존재하는 아이디인지 검증
        boolean isValid = consumptionRecordQueryService.existsConsumptionRecordById(value);
        log.info("ExistConsumptionRecord consumptionRecordId: {}, isValid: {}", value, isValid);

        if(!isValid){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.CONSUMPTION_RECORD_NOT_FOUND.toString()).addConstraintViolation();
        }

        return isValid;
    }
}
