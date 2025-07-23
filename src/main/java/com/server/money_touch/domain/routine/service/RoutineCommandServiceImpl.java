package com.server.money_touch.domain.routine.service;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.repository.budget.BudgetRepository;
import com.server.money_touch.domain.budget.repository.budgetCategory.BudgetCategoryRepository;
import com.server.money_touch.domain.routine.converter.RoutineConverter;
import com.server.money_touch.domain.routine.converter.RoutineHashtagConverter;
import com.server.money_touch.domain.routine.dto.RoutineRequest;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.domain.routine.entity.Routine;
import com.server.money_touch.domain.routine.entity.RoutineHashtag;
import com.server.money_touch.domain.routine.repository.routineHashtag.RoutineHashtagRepository;
import com.server.money_touch.domain.routine.repository.routine.RoutineRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    // 소비 루틴 등록
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

        // 4. 예산 조회 및 예산 총액/루틴 여부 수정
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.BUDGET_NOT_FOUND));
        budget.updateTotalBudget(request.getTotalBudget());

        // 5. 요청된 카테고리 이름 → 금액 맵핑
        Map<String, Integer> requestMap = request.getBudgetList().stream()
                .collect(Collectors.toMap(RoutineRequest.CategoryBudgetDTO::getCategoryName, RoutineRequest.CategoryBudgetDTO::getAmount));

        // 6. 해당 예산에 연결된 예산 카테고리 + 소비 카테고리 조회 (JOIN FETCH 필요)
        List<BudgetCategory> budgetCategories = budgetCategoryRepository.findAllByBudgetIdWithCategory(budgetId);

        // 7. categoryName → BudgetCategory 맵핑
        Map<String, BudgetCategory> budgetCategoryMap = budgetCategories.stream()
                .collect(Collectors.toMap(
                        bc -> bc.getConsumptionCategory().getBudgetCategoryName(),
                        Function.identity()
                ));

        // 8. 요청 기준으로 비교 후 금액 수정
        requestMap.forEach((categoryName, amount) -> {
            BudgetCategory category = budgetCategoryMap.get(categoryName);
            if (category == null) {
                throw new ErrorHandler(CONSUMPTION_CATEGORY_NAME_NOT_FOUND);
            }
            if (!category.getBudgetCategoryMoney().equals(amount)) {
                category.updateAmount(amount);
            }
        });

        // 9. 소비 루틴 저장
        Routine routine = RoutineConverter.toRoutine(user, budget, request);
        routineRepository.save(routine);

        // 10. 해시태그 저장
        if (request.getHashtags() != null) {
            List<RoutineHashtag> hashtags = request.getHashtags().stream()
                    .map(tag -> RoutineHashtagConverter.toRoutineHashtag(routine, tag))
                    .collect(Collectors.toList());
            routineHashtagRepository.saveAll(hashtags);
        }

        // 11. 결과 DTO 변환 후 반환
        Long routineId = routine.getId();
        log.info("소비 루틴 등록 완료 - userId: {}, routineId: {}", userId, routineId);
        return RoutineConverter.toRoutineCreateResultDTO(routineId);
    }
}
