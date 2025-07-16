package com.server.money_touch.global.validation.validator;

import com.server.money_touch.domain.budget.service.budget.BudgetQueryService;
import com.server.money_touch.domain.user.service.user.UserQueryService;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ExistBudget;
import com.server.money_touch.global.validation.annotation.ExistUser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 검증 대상은 Long
@Slf4j
@RequiredArgsConstructor
@Component
public class BudgetExistValidator implements ConstraintValidator<ExistBudget,Long> {

    private final BudgetQueryService budgetQueryService;

    @Override
    public void initialize(ExistBudget constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        // 파라미터로 넘어온 예산 아이디가 존재하는 아이디인지 검증
        boolean isValid = budgetQueryService.existsBudgetById(value);
        log.info("ExistBudget userId: {}, isValid: {}", value, isValid);

        if(!isValid){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.BUDGET_NOT_FOUND.toString()).addConstraintViolation();
        }

        return isValid;
    }
}
