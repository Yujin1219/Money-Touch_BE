package com.server.money_touch.domain.budget.service.budget;

import com.server.money_touch.domain.budget.dto.BudgetRequest;
import com.server.money_touch.domain.budget.dto.BudgetResponse;
import com.server.money_touch.global.validation.annotation.ExistUser;

public interface BudgetCommandService {
    // 예산 & 소비 카테고리 & 카테고리별 예산 등록
    BudgetResponse.BudgetCreateResultDTO saveBudgetWithCategories(@ExistUser Long userId, BudgetRequest.BudgetCreateDTO request);
}
