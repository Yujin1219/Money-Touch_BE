package com.server.money_touch.domain.budget.service.budget;

import com.server.money_touch.domain.budget.dto.BudgetRequest;
import com.server.money_touch.domain.budget.dto.BudgetResponse;
import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.global.validation.annotation.ExistUser;

import java.util.List;
import java.util.Map;

public interface BudgetCommandService {
    // 예산 수정
    BudgetResponse.BudgetCreateResultDTO saveOrUpdateBudgetWithCategories(@ExistUser Long userId, BudgetRequest.BudgetCreateDTO request);

    // 기본 & 사용자 정의 & 소비 루틴 카테고리별 예산 수정
    void updateCategoryBudgetsByType(List<? extends Object> requestList,
                                User user, Budget budget,
                                CategoryType type,
                                Map<String, BudgetCategory> existingMap);

    // 기본 & 사용자 정의 & 소비 루틴 카테고리별 예산 등록
    void saveCategoryBudgetsByType(List<? extends Object> requestList,
                                   User user, Budget budget,
                                   CategoryType type);

    Budget createOrFindBudgetForMonth(User user);
}

