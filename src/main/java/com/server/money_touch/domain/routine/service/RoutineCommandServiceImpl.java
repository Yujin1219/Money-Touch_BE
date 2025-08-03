package com.server.money_touch.domain.routine.service;

import com.server.money_touch.domain.budget.converter.budgetCategory.BudgetCategoryConverter;
import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.repository.budget.BudgetRepository;
import com.server.money_touch.domain.budget.repository.budgetCategory.BudgetCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
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
    // RoutineServiceImpl.java

    @Transactional
    @Override
    public RoutineResponse.RoutineCreateResultDTO saveRoutineWithRoutineHashtags(Long userId, Long budgetId, RoutineRequest.RoutineCreateDTO request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 1-1. 이미 이번 달에 등록된 루틴이 있는지 확인
        if (routineRepository.existsByUserIdInCurrentMonth(userId)) {
            throw new ErrorHandler(ROUTINE_ALREADY_EXIST);
        }

        // 2. 카테고리 총합 계산
        int totalCategoryBudget = Optional.ofNullable(request.getBudgetList())
                .orElse(List.of())
                .stream()
                .mapToInt(RoutineRequest.CategoryBudgetDTO::getAmount)
                .sum();

        // 3. 예산 총액과 일치 여부 확인
        if (totalCategoryBudget > request.getTotalBudget()) {
            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_EXCEEDED); // 총액 초과
        }
        if (totalCategoryBudget < request.getTotalBudget()) {
            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_TOO_LOW); // 총액 미달
        }

        // 4. 예산 존재 여부 확인 (예산과 관련된 데이터는 수정하지 않도록 구현 -> PM님 답변 오는거 보고 수정)
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.BUDGET_NOT_FOUND));

        // 5. 소비 루틴 저장
        Routine routine = Routine.builder()
                .routineName(request.getRoutineName())
                .routineImageUrl(request.getRoutineImgUrl())
                .routineTotalAmount(request.getTotalBudget())
                .budget(budget)
                .user(user)
                .build();
        routineRepository.save(routine);

        // 6. RoutineAmount 저장
        List<RoutineAmount> routineAmounts = request.getBudgetList().stream()
                .map(dto -> RoutineAmount.builder()
                        .categoryName(dto.getCategoryName())
                        .amount(dto.getAmount())
                        .routine(routine)
                        .build()
                ).collect(Collectors.toList());
        routineAmountRepository.saveAll(routineAmounts);

        // 7. 해시태그 저장
        if (request.getHashtags() != null) {
            List<RoutineHashtag> hashtags = request.getHashtags().stream()
                    .map(tag -> RoutineHashtagConverter.toRoutineHashtag(routine, tag))
                    .collect(Collectors.toList());
            routineHashtagRepository.saveAll(hashtags);
        }

        // 8. 결과 DTO 반환
        Long routineId = routine.getId();
        log.info("소비 루틴 등록 완료 - userId: {}, routineId: {}", userId, routineId);
        return RoutineConverter.toRoutineCreateResultDTO(routineId);
    }


//    @Transactional
//    @Override
//    public RoutineResponse.RoutineCreateResultDTO saveRoutineWithRoutineHashtags(Long userId, Long budgetId, RoutineRequest.RoutineCreateDTO request) {
//        // 1. 사용자 조회
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));
//
//        // 1-1. 이미 이번 달에 등록된 루틴이 있는지 확인
//        if (routineRepository.existsByUserIdInCurrentMonth(userId)) {
//            throw new ErrorHandler(ROUTINE_ALREADY_EXIST);
//        }
//
//        // 2. 카테고리 총합 계산
//        int totalCategoryBudget = Optional.ofNullable(request.getBudgetList())
//                .orElse(List.of())
//                .stream()
//                .mapToInt(RoutineRequest.CategoryBudgetDTO::getAmount)
//                .sum();
//
//        // 3. 예산 총액과 일치 여부 확인
//        if (totalCategoryBudget > request.getTotalBudget()) {
//            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_EXCEEDED); // 총액 초과
//        }
//        if (totalCategoryBudget < request.getTotalBudget()) {
//            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_TOO_LOW); // 총액 미달
//        }
//
//        // 4. 예산 조회 및 예산 총액/루틴 여부 수정
//        Budget budget = budgetRepository.findById(budgetId)
//                .orElseThrow(() -> new ErrorHandler(ErrorStatus.BUDGET_NOT_FOUND));
//        budget.updateTotalBudget(request.getTotalBudget());
//
//        // 5. 요청된 카테고리 이름 → 금액 맵핑
//        Map<String, Integer> requestMap = request.getBudgetList().stream()
//                .collect(Collectors.toMap(RoutineRequest.CategoryBudgetDTO::getCategoryName, RoutineRequest.CategoryBudgetDTO::getAmount));
//
//        // 6. 해당 예산에 연결된 예산 카테고리 + 소비 카테고리 조회
//        List<BudgetCategory> budgetCategories = budgetCategoryRepository.findAllByBudgetIdWithCategory(budgetId);
//
//        // 7. categoryName → BudgetCategory 맵핑
//        Map<String, BudgetCategory> budgetCategoryMap = budgetCategories.stream()
//                .collect(Collectors.toMap(
//                        bc -> bc.getConsumptionCategory().getBudgetCategoryName(),
//                        Function.identity()
//                ));
//
//        // 8. 요청 기준으로 비교 후 금액 수정 or 새로 생성
//        for (Map.Entry<String, Integer> entry : requestMap.entrySet()) {
//            String categoryName = entry.getKey();
//            Integer amount = entry.getValue();
//
//            BudgetCategory category = budgetCategoryMap.get(categoryName);
//
//            if (category != null) {
//                if (!category.getBudgetCategoryMoney().equals(amount)) {
//                    category.updateAmount(amount);
//                }
//            } else {
//                // 존재하지 않는 경우 CUSTOM 타입 소비 카테고리 및 예산 카테고리 생성
//                ConsumptionCategory newCategory = ConsumptionCategory.builder()
//                        .budgetCategoryName(categoryName)
//                        .budgetCategoryType(CategoryType.CUSTOM)
//                        .user(user)
//                        .build();
//
//                ConsumptionCategory savedCategory = consumptionCategoryRepository.save(newCategory);
//
//                BudgetCategory newBudgetCategory = BudgetCategory.builder()
//                        .budget(budget)
//                        .consumptionCategory(savedCategory)
//                        .budgetCategoryMoney(amount)
//                        .build();
//
//                budgetCategoryRepository.save(newBudgetCategory);
//            }
//        }

    // 타인의 소비 루틴을 내 예산에 반영
    @Transactional
    @Override
    public void applyRoutineToBudget(Long userId, Long budgetId, Long routineId, RoutineRequest.ApplyRoutineBudgetDTO request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 소비 루틴 조회 (자신의 루틴은 허용하지 않음)
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.ROUTINE_NOT_FOUND));

        if (routine.getUser().getId().equals(userId)) {
            throw new ErrorHandler(ErrorStatus.ROUTINE_PREVIEW_NOT_ALLOWED);
        }

        // 3. 예산 조회
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.BUDGET_NOT_FOUND));

        if (!budget.getUser().getId().equals(userId)) {
            throw new ErrorHandler(ErrorStatus.BUDGE_UNAUTHORIZED);
        }

        // 이미 루틴 적용된 예산이라면 예외
        if (Boolean.TRUE.equals(budget.getIsFromRoutine())) {
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
                        ConsumptionCategory newCategory = ConsumptionCategory.builder()
                                .user(user)
                                .budgetCategoryName(name)
                                .budgetCategoryType(requestType)
                                .build();
                        consumptionCategoryRepository.save(newCategory);

                        BudgetCategory newBudgetCategory = BudgetCategoryConverter.toBudgetCategory(budget, newCategory, amount);
                        budgetCategoryRepository.save(newBudgetCategory);
                    }
                });

        log.info("타인의 소비 루틴을 내 예산에 반영 완료 - userId: {}, budgetId: {}, routineId: {}", userId, budgetId, routineId);
    }
}
