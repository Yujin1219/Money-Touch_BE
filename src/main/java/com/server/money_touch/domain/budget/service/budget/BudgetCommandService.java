package com.server.money_touch.domain.budget.service.budget;

import com.server.money_touch.domain.budget.dto.BudgetRequest;
import com.server.money_touch.domain.budget.dto.BudgetResponse;
import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.global.validation.annotation.ExistUser;

import java.util.List;

public interface BudgetCommandService {
    // 예산 & 소비 카테고리 & 카테고리별 예산 등록
    BudgetResponse.BudgetCreateResultDTO saveBudgetWithCategories(@ExistUser Long userId, BudgetRequest.BudgetCreateDTO request);

    // 기본 소비 카테고리 & 카테고리별 예산 등록
    void registerDefaultCategoryBudgets(List<BudgetRequest.DefaultCategoryBudget> defaultCategoryBudgets, User user, Budget budget);

    // 사용자 정의 소비 카테고리 & 카테고리별 예산 등록
    void registerCustomCategoryBudgets(List<BudgetRequest.CustomCategoryBudget> customCategoryBudgets, User user, Budget budget);
}
