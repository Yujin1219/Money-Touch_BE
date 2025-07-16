package com.server.money_touch.domain.budget.service.budget;

import com.server.money_touch.domain.budget.converter.budget.BudgetConverter;
import com.server.money_touch.domain.budget.converter.budgetCategory.BudgetCategoryConverter;
import com.server.money_touch.domain.consumptionRecord.converter.consumptionCategory.ConsumptionCategoryConverter;
import com.server.money_touch.domain.budget.dto.BudgetRequest;
import com.server.money_touch.domain.budget.dto.BudgetResponse;
import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.budget.repository.budget.BudgetRepository;
import com.server.money_touch.domain.budget.repository.budgetCategory.BudgetCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.respotiroy.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class BudgetCommandServiceImpl implements BudgetCommandService {

    private final BudgetRepository budgetRepository;
    private final ConsumptionCategoryRepository consumptionCategoryRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final UserRepository userRepository;

    /**
     * 한 달 예산 및 카테고리별 예산을 등록합니다.
     *
     * @param userId  사용자 ID
     * @param request 예산 등록 요청 DTO
     * @return 등록된 예산 ID
     */
    @Transactional
    @Override
    public BudgetResponse.BudgetCreateResultDTO saveBudgetWithCategories(Long userId, BudgetRequest.BudgetCreateDTO request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 현재 달에 예산이 이미 등록되었는지 확인
        YearMonth now = YearMonth.now();
        LocalDateTime startOfMonth = now.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = now.atEndOfMonth().atTime(23, 59, 59);

        boolean alreadyExists = budgetRepository
                .findByUserAndCreatedAtBetween(user, startOfMonth, endOfMonth)
                .isPresent();

        if (alreadyExists) {
            throw new ErrorHandler(ErrorStatus.BUDGET_ALREADY_EXIST);
        }

        // 3. 모든 카테고리 예산 금액 합산
        int totalCategoryBudget = Stream.of(
                        request.getDefaultCategoryBudgets(),
                        request.getCustomCategoryBudgets())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .mapToInt(budget -> {
                    if (budget instanceof BudgetRequest.DefaultCategoryBudget def) return def.getAmount();
                    if (budget instanceof BudgetRequest.CustomCategoryBudget custom) return custom.getAmount();
                    return 0;
                })
                .sum();

        // 4. 전체 예산과 합계 비교
        if (totalCategoryBudget > request.getTotalBudget()) {
            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_EXCEEDED);
        }
        if (totalCategoryBudget < request.getTotalBudget()) {
            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_TOO_LOW);
        }

        // 5. 예산 엔티티 저장 (Converter 사용)
        Budget budget = BudgetConverter.toBudgetEntity(user, request);
        budgetRepository.save(budget);

        // 6. 기본 및 사용자 정의 카테고리 예산 등록
        registerDefaultCategoryBudgets(request.getDefaultCategoryBudgets(), user, budget);
        registerCustomCategoryBudgets(request.getCustomCategoryBudgets(), user, budget);

        // 7. 응답 반환
        Long budgetId = budget.getId();
        log.info("예산 등록 완료, budgetId: {}", budgetId);
        return BudgetConverter.toBudgetCreateResultDto(budgetId);
    }

    /**
     * 기본 카테고리는 사전에 등록된 것을 조회하여 연결하며,
     * 금액이 비어 있을 경우 0원으로 저장합니다.
     */
    @Transactional
    @Override
    public void registerDefaultCategoryBudgets(List<BudgetRequest.DefaultCategoryBudget> defaultCategoryBudgets, User user, Budget budget) {
        // 1. 사전 정의된 기본 카테고리 이름 목록
        List<String> defaultCategoryNames = List.of("배달/외식", "패션/쇼핑", "교통", "카페", "기타");

        // 2. 사용자에 대해 등록된 기본 카테고리 조회 (N+1 방지 위해 한 번에 조회)
        List<ConsumptionCategory> existingDefaultCategories = consumptionCategoryRepository
                .findAllByUserAndBudgetCategoryType(user, CategoryType.DEFAULT);

        // 3. 기존 카테고리를 맵으로 구성
        Map<String, ConsumptionCategory> existingCategoryMap = existingDefaultCategories.stream()
                .collect(Collectors.toMap(ConsumptionCategory::getBudgetCategoryName, c -> c));

        // 4. 아직 등록되지 않은 카테고리만 추출하여 엔티티 생성
        List<ConsumptionCategory> toBeCreated = defaultCategoryNames.stream()
                .filter(name -> !existingCategoryMap.containsKey(name))
                .map(name -> ConsumptionCategoryConverter.toConsumptionCategory(user, name, CategoryType.DEFAULT))
                .toList();

        // 5. 새로운 카테고리를 일괄 저장 (N+1 방지)
        if (!toBeCreated.isEmpty()) {
            List<ConsumptionCategory> saved = consumptionCategoryRepository.saveAll(toBeCreated);
            // 저장된 항목도 map에 추가
            saved.forEach(c -> existingCategoryMap.put(c.getBudgetCategoryName(), c));
        }

        // 6. 입력된 예산 금액을 categoryName 기준으로 Map에 구성
        Map<String, Integer> inputBudgetMap = defaultCategoryBudgets == null ? Map.of() :
                defaultCategoryBudgets.stream()
                        .collect(Collectors.toMap(BudgetRequest.DefaultCategoryBudget::getCategoryName, BudgetRequest.DefaultCategoryBudget::getAmount));

        // 7. 기본 카테고리 순서대로 BudgetCategory 생성
        List<BudgetCategory> budgetCategories = defaultCategoryNames.stream()
                .map(name -> {
                    ConsumptionCategory category = existingCategoryMap.get(name);
                    int amount = inputBudgetMap.getOrDefault(name, 0); // 입력 없으면 0원
                    return BudgetCategoryConverter.toBudgetCategory(budget, category, amount);
                })
                .toList();

        // 8. 예산-카테고리 항목을 일괄 저장
        budgetCategoryRepository.saveAll(budgetCategories);
    }

    /**
     * 사용자 정의 카테고리 및 카테고리 예산을 저장합니다.
     */
    @Transactional
    @Override
    public void registerCustomCategoryBudgets(List<BudgetRequest.CustomCategoryBudget> customCategoryBudgets, User user, Budget budget) {
        if (customCategoryBudgets == null || customCategoryBudgets.isEmpty()) return;

        // 기존 사용자 정의 카테고리 조회
        Map<String, ConsumptionCategory> existingCategoryMap = customCategoryBudgets.stream()
                .map(dto -> dto.getCategoryName())
                .distinct()
                .map(name -> consumptionCategoryRepository
                        .findByUserAndBudgetCategoryNameAndBudgetCategoryType(user, name, CategoryType.CUSTOM)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ConsumptionCategory::getBudgetCategoryName, c -> c));

        // 새로 만들어야 하는 카테고리
        List<ConsumptionCategory> newCategories = customCategoryBudgets.stream()
                .map(BudgetRequest.CustomCategoryBudget::getCategoryName)
                .filter(name -> !existingCategoryMap.containsKey(name))
                .distinct()
                .map(name -> ConsumptionCategoryConverter.toConsumptionCategory(user, name, CategoryType.CUSTOM))
                .collect(Collectors.toList());

        if (!newCategories.isEmpty()) {
            consumptionCategoryRepository.saveAll(newCategories);
            newCategories.forEach(c -> existingCategoryMap.put(c.getBudgetCategoryName(), c));
        }

        // 예산-카테고리 연결
        List<BudgetCategory> budgetCategories = customCategoryBudgets.stream()
                .map(dto -> {
                    ConsumptionCategory category = existingCategoryMap.get(dto.getCategoryName());
                    return BudgetCategoryConverter.toBudgetCategory(budget, category, dto.getAmount());
                })
                .collect(Collectors.toList());

        budgetCategoryRepository.saveAll(budgetCategories);
    }
}
