package com.server.money_touch.domain.routine.service;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.budget.repository.budgetCategory.BudgetCategoryRepository;
import com.server.money_touch.domain.routine.converter.RoutineConverter;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.domain.routine.entity.Routine;
import com.server.money_touch.domain.routine.repository.routine.RoutineRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class RoutineQueryServiceImpl implements RoutineQueryService {
    private static final Integer PAGE_SIZE = 10;

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;

    // 소비 루틴 존재 여부 검증
    @Override
    public Boolean existsRoutineById(Long routineId) {
        return routineRepository.findById(routineId).isPresent();
    }

    // 내 소비 루틴 상세 조회
    @Override
    public RoutineResponse.RoutineDetailDTO getUserRoutineDetail(Long userId, Long routineId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 루틴 조회 (userId와 routineId 일치 여부 확인)
        Routine routine = routineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.ROUTINE_NOT_FOUND));

        Budget budget = routine.getBudget();

        // 3. 예산-카테고리 목록 조회
        List<BudgetCategory> budgetCategories = budgetCategoryRepository.findAllWithCategoryByBudgetId(budget.getId());

        // 4. categoryType별로 분류
        Map<CategoryType, List<BudgetCategory>> groupedByType = budgetCategories.stream()
                .collect(Collectors.groupingBy(bc -> bc.getConsumptionCategory().getBudgetCategoryType()));

        // 5. CategoryBudgetDetailDTO 변환 (기본 → 커스텀 → 루틴 순서 유지)
        List<RoutineResponse.CategoryBudgetDetailDTO> categoryBudgetList = Stream.of(
                        CategoryType.DEFAULT,
                        CategoryType.CUSTOM,
                        CategoryType.ROUTINE_CATEGORY
                )
                .flatMap(type -> groupedByType.getOrDefault(type, Collections.emptyList()).stream())
                .map(bc -> RoutineResponse.CategoryBudgetDetailDTO.builder()
                        .categoryName(bc.getConsumptionCategory().getBudgetCategoryName())
                        .amount(bc.getBudgetCategoryMoney())
                        .build())
                .collect(Collectors.toList());

        log.info("내 소비 루틴 상세 조회 완료 - userId: {}, routeIneId: {}", userId, routineId);
        return RoutineConverter.toRoutineDetailDTO(budget.getBudgetTotal(), routine.getRoutineName(), categoryBudgetList);
    }

    // 내 소비 루틴 목록 조회 (커서 기반 무한스크롤)
    @Override
    public RoutineResponse.MyRoutineListDTO getMyRoutineList(Long userId, Long cursorId) {
        // 1. Pageable 객체 생성 (페이지 크기 고정, 0페이지부터 시작)
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        // 2. 커서 기반으로 루틴 목록 조회 (Slice 사용)
        Slice<RoutineResponse.RoutineThumbnailDTO> slice = routineRepository.findUserRoutineList(userId, cursorId, pageable);

        // 3. 조회된 루틴 리스트 추출
        List<RoutineResponse.RoutineThumbnailDTO> routineList = slice.getContent();

        log.info("내 소비 루틴 목록 조회(커서 기반 무한스크롤) 완료 - userId: {}, cursorId: {}", userId, cursorId);
        return RoutineConverter.toMyRoutineListDTO(routineList, slice);
    }
}
