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
    public BudgetResponse.BudgetCreateResultDTO saveOrUpdateBudgetWithCategories(Long userId, Integer year, Integer month, BudgetRequest.BudgetCreateDTO request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 파라미터 기준 예산 조회 (예: 2025-07)
        String createdMonth = String.format("%d-%02d", year, month); // createdMonth 문자열로 변환 ("2025-07")
        Optional<Budget> optionalBudget = budgetRepository.findByUserAndCreatedMonth(user, createdMonth);

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

            // 소비 루틴 카테고리는 타인의 소비 루틴을 내 예산에 반영하여 소비 루틴 카테고리가 생성된 경우만 가능
            if (hasRoutineCategoryBudget(request)) {
                boolean hasRoutineData = consumptionCategoryRepository.existsByUserAndBudgetCategoryType(user, CategoryType.ROUTINE_CATEGORY);

                if (!hasRoutineData) {
                    throw new ErrorHandler(ErrorStatus.ROUTINE_CATEGORY_NOT_ALLOWED);
                }
            }
            updateCategoryBudgetsByType(request.getRoutineCategoryBudgets(), user, budget, CategoryType.ROUTINE_CATEGORY, existingMapByType.getOrDefault(CategoryType.ROUTINE_CATEGORY, Map.of()));

            log.info("예산 수정 완료 - userId: {}, budgetId: {}", userId, budget.getId());
        } else {
            // 5-B. 예산 없음: 새로 등록
            budget = BudgetConverter.toBudgetEntity(user, request.getTotalBudget(), createdMonth);
            budgetRepository.save(budget);

            saveCategoryBudgetsByType(request.getDefaultCategoryBudgets(), user, budget, CategoryType.DEFAULT);
            saveCategoryBudgetsByType(request.getCustomCategoryBudgets(), user, budget, CategoryType.CUSTOM);

            // 소비 루틴 카테고리는 타인의 소비 루틴을 내 예산에 반영하여 소비 루틴 카테고리가 생성된 경우만 가능
            if (hasRoutineCategoryBudget(request)) {
                boolean hasRoutineData = consumptionCategoryRepository.existsByUserAndBudgetCategoryType(user, CategoryType.ROUTINE_CATEGORY);

                if (!hasRoutineData) {
                    throw new ErrorHandler(ErrorStatus.ROUTINE_CATEGORY_NOT_ALLOWED);
                }
//                saveCategoryBudgetsByType(request.getRoutineCategoryBudgets(), user, budget, CategoryType.ROUTINE_CATEGORY);
            }

            log.info("예산 등록 완료 - userId: {}, budgetId: {}", user.getId(), budget.getId());
        }

        return BudgetConverter.toBudgetCreateResultDto(budget.getId(), budget.getBudgetTotal());
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
            // ✅ 기본 카테고리 순서 유지를 위해 LinkedHashMap 사용
            Map<String, Integer> nameToAmount = new LinkedHashMap<>();

            // 요청된 이름-금액 쌍 추출
            Optional.ofNullable(requestList).orElse(List.of()).stream()
                    .filter(Objects::nonNull)
                    .map(dto -> {
                        if (dto instanceof BudgetRequest.DefaultCategoryBudget def)
                            return Map.entry(def.getCategoryName(), def.getAmount());
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .forEach(entry -> nameToAmount.put(entry.getKey(), entry.getValue()));

            // ✅ 유효하지 않은 이름이 포함된 경우 예외 발생
            boolean hasInvalidName = nameToAmount.keySet().stream()
                    .anyMatch(name -> !DefaultCategoryConstants.DEFAULT_CATEGORY_NAMES.contains(name));
            if (hasInvalidName) {
                throw new ErrorHandler(ErrorStatus.CONSUMPTION_CATEGORY_TYPE_NOT_FOUND);
            }

            // ✅ 누락된 기본 카테고리 중 기존에도 없는 항목은 0원으로 보정 (순서 유지)
            DefaultCategoryConstants.DEFAULT_CATEGORY_NAMES.forEach(name -> {
                if (!nameToAmount.containsKey(name) && !existingMap.containsKey(name)) {
                    nameToAmount.put(name, 0);
                }
            });

            // ✅ 이미 존재하는 항목은 제거
            nameToAmount.keySet().removeIf(existingMap::containsKey);

            // ✅ 실제 추가/갱신할 항목이 없는 경우 종료
            if (nameToAmount.isEmpty()) {
                log.info("[카테고리 등록] 모든 기본 카테고리가 이미 존재하여 저장할 항목이 없습니다. userId={}, budgetId={}", user.getId(), budget.getId());
                return;
            }

            // ✅ 공통 저장 로직 호출
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

        // 1. 기본 타입(DEFAULT)인 경우 누락된 항목을 0원으로 보정
        if (type == CategoryType.DEFAULT) {
            for (String defaultName : DefaultCategoryConstants.DEFAULT_CATEGORY_NAMES) {
                if (!nameToAmount.containsKey(defaultName)) {
                    nameToAmount.put(defaultName, 0);
                }
            }
        }

        // 2. 저장 또는 업데이트할 BudgetCategory 리스트 생성
        List<BudgetCategory> toSave = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : nameToAmount.entrySet()) {
            String name = entry.getKey();
            Integer amount = entry.getValue();

            BudgetCategory existing = existingMap.get(name);
            if (existing != null) {
                existing.updateAmount(amount);
                continue; // 업데이트 완료
            }

            ConsumptionCategory category = consumptionCategoryRepository
                    .findByUserAndBudgetCategoryNameAndBudgetCategoryType(user, name, type)
                    .orElseGet(() -> consumptionCategoryRepository.save(
                            ConsumptionCategoryConverter.toConsumptionCategory(user, name, type)
                    ));

            toSave.add(BudgetCategoryConverter.toBudgetCategory(budget, category, amount));
        }

        // 3. 새 BudgetCategory 저장
        if (!toSave.isEmpty()) {
            budgetCategoryRepository.saveAll(toSave);
        }

        // 4. DEFAULT가 아닐 경우, 요청에 없는 기존 BudgetCategory 삭제
        if (type != CategoryType.DEFAULT) {
            Set<String> requestedNames = nameToAmount.keySet();
            List<BudgetCategory> toDelete = existingMap.entrySet().stream()
                    .filter(entry -> !requestedNames.contains(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .toList();

            if (!toDelete.isEmpty()) {
                log.info("[카테고리 삭제] 예산 ID={}, 타입={}, 삭제 대상(BudgetCategory)={}",
                        budget.getId(), type.name(),
                        toDelete.stream().map(BudgetCategory::getId).toList());

                budgetCategoryRepository.deleteAllInBatch(toDelete);

                // 연관된 ConsumptionCategory도 고아이면 삭제
                List<Long> categoryIds = toDelete.stream()
                        .map(bc -> bc.getConsumptionCategory().getId())
                        .distinct()
                        .toList();

                List<ConsumptionCategory> orphaned = consumptionCategoryRepository.findAllById(categoryIds).stream()
                        .filter(cat -> budgetCategoryRepository.countByConsumptionCategory(cat) == 0)
                        .toList();

                if (!orphaned.isEmpty()) {
                    log.info("[카테고리 삭제] 삭제 대상(ConsumptionCategory)={}",
                            orphaned.stream().map(ConsumptionCategory::getId).toList());
                    consumptionCategoryRepository.deleteAllInBatch(orphaned);
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
        String createdMonth = String.format("%d-%02d", now.getYear(), now.getMonthValue()); // ← 수정된 부분
        Budget newBudget = BudgetConverter.toBudgetEntity(user, 0, createdMonth);

        return budgetRepository.save(newBudget);
    }

    private boolean hasRoutineCategoryBudget(BudgetRequest.BudgetCreateDTO request) {
        return request.getRoutineCategoryBudgets() != null && !request.getRoutineCategoryBudgets().isEmpty();
    }
}
