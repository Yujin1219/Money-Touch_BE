package com.server.money_touch.domain.routine.service;

import com.server.money_touch.domain.budget.converter.budgetCategory.BudgetCategoryConverter;
import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.repository.budget.BudgetRepository;
import com.server.money_touch.domain.budget.repository.budgetCategory.BudgetCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.converter.consumptionCategory.ConsumptionCategoryConverter;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
import com.server.money_touch.domain.routine.converter.RoutineAmountConverter;
import com.server.money_touch.domain.routine.converter.RoutineConverter;
import com.server.money_touch.domain.routine.converter.RoutineHashtagConverter;
import com.server.money_touch.domain.routine.dto.RoutineRequest;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.domain.routine.entity.Routine;
import com.server.money_touch.domain.routine.entity.RoutineAmount;
import com.server.money_touch.domain.routine.entity.RoutineHashtag;
import com.server.money_touch.domain.routine.repository.routine.RoutineAmountRepository;
import com.server.money_touch.domain.routine.repository.routineHashtag.RoutineHashtagRepository;
import com.server.money_touch.domain.routine.repository.routine.RoutineRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import com.server.money_touch.domain.budget.enums.CategoryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.server.money_touch.global.apiPayload.code.status.ErrorStatus.*;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RoutineCommandServiceImpl implements RoutineCommandService {

    private final RoutineRepository routineRepository;
    private final RoutineHashtagRepository routineHashtagRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final ConsumptionCategoryRepository consumptionCategoryRepository;
    private final RoutineAmountRepository routineAmountRepository;

    // 소비 루틴 등록
    @Transactional
    @Override
    public RoutineResponse.RoutineCreateResultDTO saveRoutineWithRoutineHashtags(
            Long userId,
            Long budgetId,
            RoutineRequest.RoutineCreateDTO request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 예산 조회
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.BUDGET_NOT_FOUND));

        // 3. 예산의 createdMonth로 이번 달 판단
        String createdMonth = budget.getCreatedMonth();

        // 4. 이번 달 동일 예산에 대한 루틴 존재 여부 확인 (단, userId=1 은 중복 허용)
        if (!userId.equals(1L)) { // ✅ userId=1일 때는 중복 검사 스킵
            if (routineRepository.findForUpdateByUserAndBudgetAndMonth(userId, budgetId, createdMonth).isPresent()) {
                throw new ErrorHandler(ROUTINE_ALREADY_EXIST);
            }
        }

        // 5. 카테고리 총합 계산
        int totalCategoryBudget = Optional.ofNullable(request.getBudgetList())
                .orElse(List.of())
                .stream()
                .mapToInt(RoutineRequest.CategoryBudgetDTO::getAmount)
                .sum();

        // 6. 예산 총액과 일치 여부 확인
        if (totalCategoryBudget > request.getTotalBudget()) {
            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_EXCEEDED);
        }
        if (totalCategoryBudget < request.getTotalBudget()) {
            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_TOO_LOW);
        }

        // 7. 루틴 저장
        Routine routine = RoutineConverter.toRoutine(user, budget, request, createdMonth);
        try {
            routineRepository.save(routine);
        } catch (Exception e) {
            // 혹시 경쟁 상황으로 유니크 제약 위반 시
            if (!userId.equals(1L)) { // ✅ userId=1일 때는 예외 변환하지 않음
                throw new ErrorHandler(ROUTINE_ALREADY_EXIST);
            }
        }

        // 8. RoutineAmount 저장
        List<RoutineAmount> routineAmounts = request.getBudgetList().stream()
                .map(dto -> RoutineAmountConverter.toRoutineAmount(dto, routine))
                .collect(Collectors.toList());
        routineAmountRepository.saveAll(routineAmounts);

        // 9. 해시태그 저장 (최대 3개까지만)
        if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
            List<RoutineHashtag> hashtags = request.getHashtags().stream()
                    .limit(3) // ✅ 최대 3개 제한
                    .map(tag -> RoutineHashtagConverter.toRoutineHashtag(routine, tag))
                    .collect(Collectors.toList());
            routineHashtagRepository.saveAll(hashtags);
        }

        // 10. 결과 반환
        return RoutineConverter.toRoutineCreateResultDTO(routine.getId());
    }

    // 타인의 소비 루틴을 내 예산에 반영
    @Transactional
    @Override
    public void applyRoutineToBudget(Long userId, Long routineId, RoutineRequest.ApplyRoutineBudgetDTO request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 소비 루틴 조회 (자신의 루틴은 허용하지 않음)
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.ROUTINE_NOT_FOUND));

        if (routine.getUser().getId().equals(userId)) {
            throw new ErrorHandler(ErrorStatus.ROUTINE_PREVIEW_NOT_ALLOWED);
        }

        // 3. 이번 달 예산 조회
        String currentMonth = LocalDate.now().withDayOfMonth(1).toString().substring(0, 7); // "2025-08"
        Budget budget = budgetRepository.findByUserIdAndCreatedMonth(userId, currentMonth)
                .orElseThrow(() -> new ErrorHandler(BUDGET_NOT_EXIST));

        if (!budget.getUser().getId().equals(userId)) {
            throw new ErrorHandler(ErrorStatus.BUDGE_UNAUTHORIZED);
        }

        // 이미 루틴 적용된 예산이라면 예외 (단, userId=1은 중복 허용)
        if (!userId.equals(1L) && Boolean.TRUE.equals(budget.getIsFromRoutine())) {
            throw new ErrorHandler(ErrorStatus.ROUTINE_ALREADY_APPLIED);
        }

        // 4. 총 카테고리 예산 합계 계산
        int totalCategoryBudget = Stream.of(
                        Optional.ofNullable(request.getDefaultCategoryBudgets()).orElse(List.of()),
                        Optional.ofNullable(request.getCustomCategoryBudgets()).orElse(List.of()),
                        Optional.ofNullable(request.getRoutineCategoryBudgets()).orElse(List.of()))
                .flatMap(List::stream)
                .mapToInt(RoutineRequest.ApplyCategoryBudgetDTO::getAmount)
                .sum();

        // 5. 총 예산과 일치 여부 검증
        if (totalCategoryBudget > request.getTotalBudget()) {
            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_EXCEEDED);
        }
        if (totalCategoryBudget < request.getTotalBudget()) {
            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_TOO_LOW);
        }

        // 6. 총 예산 반영 및 루틴 플래그 true 처리
        budget.updateTotalBudget(request.getTotalBudget());
        budget.updateIsFromRoutine(true);

        // 7. 기존 예산 카테고리 조회 → Map<String, BudgetCategory>
        List<BudgetCategory> myCategories = budgetCategoryRepository.findByBudgetWithConsumptionCategory(budget);
        Map<String, BudgetCategory> myCategoryMap = myCategories.stream()
                .collect(Collectors.toMap(
                        bc -> bc.getConsumptionCategory().getBudgetCategoryName(),
                        Function.identity()
                ));

        // 8. 요청 카테고리 전체 순회 및 반영
        Stream.of(
                        Optional.ofNullable(request.getDefaultCategoryBudgets()).orElse(List.of()),
                        Optional.ofNullable(request.getCustomCategoryBudgets()).orElse(List.of()),
                        Optional.ofNullable(request.getRoutineCategoryBudgets()).orElse(List.of())
                )
                .flatMap(List::stream)
                .forEach(dto -> {
                    String name = dto.getCategoryName();
                    Integer amount = dto.getAmount();
                    CategoryType requestType = dto.getCategoryType();

                    if (myCategoryMap.containsKey(name)) {
                        // ✅ 기존 항목이 있는 경우 → 금액 갱신 및 타입 필요 시 갱신
                        BudgetCategory existing = myCategoryMap.get(name);
                        existing.updateAmount(amount);

                        ConsumptionCategory category = existing.getConsumptionCategory();
                        if (category.getBudgetCategoryType() != requestType) {
                            category.updateCategoryType(requestType);
                        }

                    } else {
                        // ✅ 기존 항목이 없는 경우 → 새로 생성
                        ConsumptionCategory newCategory = ConsumptionCategoryConverter.toConsumptionCategory(user, name, requestType);
                        consumptionCategoryRepository.save(newCategory);

                        BudgetCategory newBudgetCategory = BudgetCategoryConverter.toBudgetCategory(budget, newCategory, amount);
                        budgetCategoryRepository.save(newBudgetCategory);
                    }
                });

        log.info("타인의 소비 루틴을 내 예산에 반영 완료 - userId: {}, currentMonth: {}, routineId: {}", userId, currentMonth, routineId);
    }
}
