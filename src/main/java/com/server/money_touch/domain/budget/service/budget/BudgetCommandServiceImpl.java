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
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import com.server.money_touch.global.constants.DefaultCategoryConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.*;
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

    // 예산 등록 또는 수정
    @Transactional
    @Override
    public BudgetResponse.BudgetCreateResultDTO saveOrUpdateBudgetWithCategories(Long userId, BudgetRequest.BudgetCreateDTO request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 이번 달 기준 예산 조회
        YearMonth now = YearMonth.now();
        LocalDateTime start = now.atDay(1).atStartOfDay(); // 월 시작 00:00:00
        LocalDateTime end = now.atEndOfMonth().atTime(LocalTime.MAX);

        Optional<Budget> optionalBudget = budgetRepository.findByUserAndCreatedAtBetween(user, start, end);

        // 3. 카테고리 총합 계산
        int totalCategoryBudget = Stream.of(
                        Optional.ofNullable(request.getDefaultCategoryBudgets()).orElse(List.of()),
                        Optional.ofNullable(request.getCustomCategoryBudgets()).orElse(List.of()),
                        Optional.ofNullable(request.getRoutineCategoryBudgets()).orElse(List.of()))
                .flatMap(List::stream)
                .mapToInt(budgetDTO -> {
                    if (budgetDTO instanceof BudgetRequest.DefaultCategoryBudget def) return def.getAmount();
                    if (budgetDTO instanceof BudgetRequest.CustomCategoryBudget custom) return custom.getAmount();
                    if (budgetDTO instanceof BudgetRequest.RoutineCategoryBudget routine) return routine.getAmount();
                    return 0;
                })
                .sum();

        // 4. 예산 총액과 일치 여부 확인
        if (totalCategoryBudget > request.getTotalBudget()) {
            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_EXCEEDED);
        }
        if (totalCategoryBudget < request.getTotalBudget()) {
            throw new ErrorHandler(ErrorStatus.TOTAL_BUDGET_TOO_LOW);
        }

        Budget budget;

        if (optionalBudget.isPresent()) {
            // 5-A. 예산 존재: 수정
            budget = optionalBudget.get();
            budget.updateTotalBudget(request.getTotalBudget());

            List<BudgetCategory> allBudgetCategories = budgetCategoryRepository.findAllWithCategoryByBudget(budget);

            Map<CategoryType, Map<String, BudgetCategory>> existingMapByType = allBudgetCategories.stream()
                    .collect(Collectors.groupingBy(
                            bc -> bc.getConsumptionCategory().getBudgetCategoryType(),
                            Collectors.toMap(
                                    bc -> bc.getConsumptionCategory().getBudgetCategoryName(),
                                    bc -> bc
                            )
                    ));

            updateCategoryBudgetsByType(request.getDefaultCategoryBudgets(), user, budget, CategoryType.DEFAULT, existingMapByType.getOrDefault(CategoryType.DEFAULT, Map.of()));
            updateCategoryBudgetsByType(request.getCustomCategoryBudgets(), user, budget, CategoryType.CUSTOM, existingMapByType.getOrDefault(CategoryType.CUSTOM, Map.of()));
            updateCategoryBudgetsByType(request.getRoutineCategoryBudgets(), user, budget, CategoryType.ROUTINE_CATEGORY, existingMapByType.getOrDefault(CategoryType.ROUTINE_CATEGORY, Map.of()));

            log.info("예산 수정 완료, budgetId: {}", budget.getId());
        } else {
            // 5-B. 예산 없음: 새로 등록
            budget = BudgetConverter.toBudgetEntity(user, request.getTotalBudget());
            budgetRepository.save(budget);

            saveCategoryBudgetsByType(request.getDefaultCategoryBudgets(), user, budget, CategoryType.DEFAULT);
            saveCategoryBudgetsByType(request.getCustomCategoryBudgets(), user, budget, CategoryType.CUSTOM);
            saveCategoryBudgetsByType(request.getRoutineCategoryBudgets(), user, budget, CategoryType.ROUTINE_CATEGORY);

            log.info("예산 등록 완료, budgetId: {}", budget.getId());
        }

        return BudgetConverter.toBudgetCreateResultDto(budget.getId());
    }

    // 기본, 커스텀, 소비루틴 카테고리 수정
    @Transactional
    @Override
    public void updateCategoryBudgetsByType(List<? extends Object> requestList,
                                            User user, Budget budget,
                                            CategoryType type,
                                            Map<String, BudgetCategory> existingMap) {
        saveOrUpdateCategoryBudgetsByType(requestList, user, budget, type, existingMap);
    }

    // 기본, 커스텀, 소비루틴 카테고리 등록
    @Override
    @Transactional
    public void saveCategoryBudgetsByType(List<? extends Object> requestList,
                                          User user, Budget budget,
                                          CategoryType type) {
        List<BudgetCategory> existingList = budgetCategoryRepository.findAllWithCategoryByBudgetAndType(budget, type);
        Map<String, BudgetCategory> existingMap = existingList.stream()
                .collect(Collectors.toMap(
                        bc -> bc.getConsumptionCategory().getBudgetCategoryName(),
                        bc -> bc
                ));

        // DEFAULT 타입일 경우만 유효성 검사 및 누락 보정 적용
        if (type == CategoryType.DEFAULT) {
            // 요청된 이름-금액 쌍 추출
            Map<String, Integer> nameToAmount = Optional.ofNullable(requestList).orElse(List.of()).stream()
                    .filter(Objects::nonNull)
                    .map(dto -> {
                        if (dto instanceof BudgetRequest.DefaultCategoryBudget def)
                            return Map.entry(def.getCategoryName(), def.getAmount());
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // 예외 발생: 요청에 기본 카테고리 외의 이름이 포함된 경우
            boolean hasInvalidName = nameToAmount.keySet().stream()
                    .anyMatch(name -> !DefaultCategoryConstants.DEFAULT_CATEGORY_NAMES.contains(name));
            if (hasInvalidName) {
                throw new ErrorHandler(ErrorStatus.CONSUMPTION_CATEGORY_TYPE_NOT_FOUND);
            }

            // 누락된 기본 카테고리 이름 중, 기존에 존재하지 않는 항목만 0원으로 보정
            DefaultCategoryConstants.DEFAULT_CATEGORY_NAMES.stream()
                    .filter(defaultName -> !nameToAmount.containsKey(defaultName)) // 요청에 없는 이름
                    .filter(defaultName -> !existingMap.containsKey(defaultName)) // 기존에도 없는 이름
                    .forEach(missing -> nameToAmount.put(missing, 0));

            // 요청에 포함되었지만 이미 존재하는 항목은 제외
            nameToAmount.keySet().removeIf(existingMap::containsKey);

            // 실제 추가/갱신할 항목이 없는 경우 종료
            if (nameToAmount.isEmpty()) {
                log.info("[카테고리 등록] 모든 기본 카테고리가 이미 존재하여 저장할 항목이 없습니다. userId={}, budgetId={}", user.getId(), budget.getId());
                return;
            }

            // 수정 공통 메서드 호출
            saveOrUpdateCategoryBudgetsByNameMap(nameToAmount, user, budget, type, existingMap);
        } else {
            // 그 외 타입은 기존 처리 방식 유지
            saveOrUpdateCategoryBudgetsByType(requestList, user, budget, type, existingMap);
        }
    }

    /**
     * BudgetCategory를 저장 또는 수정하는 공통 로직.
     * - 요청된 DTO 리스트에서 이름과 금액을 추출한 후
     * - 기존 BudgetCategory 존재 여부에 따라 수정 또는 새로 생성
     *
     * @param requestList  카테고리별 DTO 목록 (Default, Custom, Routine)
     * @param user         사용자
     * @param budget       예산 엔티티
     * @param type         카테고리 타입 (DEFAULT, CUSTOM, ROUTINE_CATEGORY)
     * @param existingMap  이미 저장된 BudgetCategory의 이름 → 엔티티 매핑 맵
     */
    @Transactional
    void saveOrUpdateCategoryBudgetsByType(List<? extends Object> requestList,
                                           User user,
                                           Budget budget,
                                           CategoryType type,
                                           Map<String, BudgetCategory> existingMap) {

        // 1. 요청된 DTO에서 카테고리 이름과 금액을 추출하여 Map 생성
        // null 또는 비어있을 경우 삭제만 처리되도록 빈 맵으로 처리
        Map<String, Integer> nameToAmount = Optional.ofNullable(requestList).orElse(List.of()).stream()
                .map(dto -> {
                    if (dto instanceof BudgetRequest.DefaultCategoryBudget def)
                        return Map.entry(def.getCategoryName(), def.getAmount());
                    if (dto instanceof BudgetRequest.CustomCategoryBudget custom)
                        return Map.entry(custom.getCategoryName(), custom.getAmount());
                    if (dto instanceof BudgetRequest.RoutineCategoryBudget routine)
                        return Map.entry(routine.getCategoryName(), routine.getAmount());
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 2. 실제 저장 또는 수정 로직 위임
        saveOrUpdateCategoryBudgetsByNameMap(nameToAmount, user, budget, type, existingMap);
    }

    /**
     * 카테고리 이름과 금액 맵 기반으로 BudgetCategory 저장 또는 수정
     * - 기존 BudgetCategory가 있으면 금액만 수정
     * - 없으면 ConsumptionCategory를 조회 또는 생성 후 BudgetCategory 생성
     *
     * @param nameToAmount 카테고리 이름 → 금액 매핑
     * @param user         사용자
     * @param budget       예산 엔티티
     * @param type         카테고리 타입
     * @param existingMap  기존 BudgetCategory 엔티티 맵 (이름 → BudgetCategory)
     */
    @Transactional
    void saveOrUpdateCategoryBudgetsByNameMap(Map<String, Integer> nameToAmount,
                                              User user,
                                              Budget budget,
                                              CategoryType type,
                                              Map<String, BudgetCategory> existingMap) {

        // 1. 각 이름-금액 쌍에 대해 저장 또는 수정 처리
        List<BudgetCategory> toSave = nameToAmount.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    Integer amount = entry.getValue();

                    // 이미 존재하는 BudgetCategory가 있으면 금액만 업데이트
                    BudgetCategory existing = existingMap.get(name);
                    if (existing != null) {
                        existing.updateAmount(amount);
                        return null; // 업데이트만, 새로 저장할 건 아님
                    }

                    // 없으면 ConsumptionCategory를 찾거나 생성하고 BudgetCategory 생성
                    ConsumptionCategory category = consumptionCategoryRepository
                            .findByUserAndBudgetCategoryNameAndBudgetCategoryType(user, name, type)
                            .orElseGet(() -> consumptionCategoryRepository.save(
                                    ConsumptionCategoryConverter.toConsumptionCategory(user, name, type)
                            ));
                    return BudgetCategoryConverter.toBudgetCategory(budget, category, amount);
                })
                .filter(Objects::nonNull) // 새로 생성된 것만 저장 대상
                .toList();

        // 2. 새로 생성된 BudgetCategory 저장
        if (!toSave.isEmpty()) {
            budgetCategoryRepository.saveAll(toSave);
        }

        // 3. 삭제 대상 처리 (요청에 포함되지 않은 기존 BudgetCategory 삭제)
        // 요청된 nameToAmount에 없는 기존 항목은 삭제 대상이 됨
        // 단, DEFAULT 타입은 삭제 금지
        if (type != CategoryType.DEFAULT) { // 기본 카테고리는 삭제 금지

            Set<String> requestedNames = (nameToAmount != null) ? nameToAmount.keySet() : Set.of();

            List<BudgetCategory> toDelete = existingMap.entrySet().stream()
                    .filter(entry -> !requestedNames.contains(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .toList();

            if (!toDelete.isEmpty()) {
                log.info("[카테고리 삭제] 예산 ID={}, 타입={}, 삭제 대상(BudgetCategory)={}",
                        budget.getId(), type.name(),
                        toDelete.stream().map(BudgetCategory::getId).toList());

                // 먼저 BudgetCategory 삭제
                budgetCategoryRepository.deleteAllInBatch(toDelete);

                // BudgetCategory 삭제 후 연결된 ConsumptionCategory도 고아이면 삭제
                List<Long> categoryIds = toDelete.stream()
                        .map(bc -> bc.getConsumptionCategory().getId())
                        .distinct()
                        .toList();

                // 고아 상태인지 확인 후 삭제
                List<ConsumptionCategory> consumptionToDelete = consumptionCategoryRepository
                        .findAllById(categoryIds)
                        .stream()
                        .filter(cat -> budgetCategoryRepository.countByConsumptionCategory(cat) == 0)
                        .toList();

                if (!consumptionToDelete.isEmpty()) {
                    log.info("[카테고리 삭제] 삭제 대상(ConsumptionCategory)={}",
                            consumptionToDelete.stream().map(ConsumptionCategory::getId).toList());
                    consumptionCategoryRepository.deleteAllInBatch(consumptionToDelete);
                }
            }
        }
    }

    @Transactional
    @Override
    public Budget createOrFindBudgetForMonth(User user) {
        // 이번 달의 연도와 월 구하기
        YearMonth now = YearMonth.now();
        LocalDateTime start = now.atDay(1).atStartOfDay(); // 월 시작 00:00:00
        LocalDateTime end = now.atEndOfMonth().atTime(LocalTime.MAX);

        // 이미 등록된 Budget이 있는지 확인
        Budget existing = budgetRepository.findByUserAndCreatedAtBetween(user, start, end).orElse(null);
        if (existing != null) {
            return existing;
        }

        // 없다면 새로 생성
        Budget newBudget = BudgetConverter.toBudgetEntity(user, 0);

        return budgetRepository.save(newBudget);
    }
}
