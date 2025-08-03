package com.server.money_touch.domain.routine.service;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.budget.repository.budget.BudgetRepository;
import com.server.money_touch.domain.budget.repository.budgetCategory.BudgetCategoryRepository;
import com.server.money_touch.domain.routine.converter.RoutineConverter;
import com.server.money_touch.domain.routine.dto.RoutineResponse;
import com.server.money_touch.domain.routine.entity.Routine;
import com.server.money_touch.domain.routine.entity.RoutineAmount;
import com.server.money_touch.domain.routine.repository.routine.RoutineAmountRepository;
import com.server.money_touch.domain.routine.repository.routine.RoutineRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import com.server.money_touch.global.constants.DefaultCategoryConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
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
    private final BudgetRepository budgetRepository;
    private final RoutineAmountRepository routineAmountRepository;

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

        // 3. 루틴 금액 정보 조회
        List<RoutineAmount> routineAmounts = routineAmountRepository.findAllWithRoutineByRoutineId(routineId);

        // 4. 카테고리 정보 DTO로 변환
        List<RoutineResponse.CategoryBudgetDetailDTO> categoryBudgetList = routineAmounts.stream()
                .map(ra -> RoutineResponse.CategoryBudgetDetailDTO.builder()
                        .categoryName(ra.getCategoryName())
                        .amount(ra.getAmount())
                        .build())
                .collect(Collectors.toList());

        log.info("내 소비 루틴 상세 조회 완료 - userId: {}, routeIneId: {}", userId, routineId);
        return RoutineConverter.toRoutineDetailDTO(routine.getRoutineTotalAmount(), routine.getRoutineName(), categoryBudgetList);
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

    @Override
    public RoutineResponse.AllRoutineListDTO getAllRoutineList(Long cursorId) {

        // 페이지 크기 10, 0페이지부터 시작
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        // Repository 호출 → 커서 기반 + 해시태그 + NEW 여부
        Slice<RoutineResponse.RoutineListDTO> slice = routineRepository.findAllRoutines(cursorId, pageable);

        // 조회한 루틴 리스트 추출
        List<RoutineResponse.RoutineListDTO> routineList = slice.getContent();

        log.info("전체 소비 루틴 목록 조회(커서 기반 무한스크롤) 완료 - cursorId: {}", cursorId);
        return RoutineConverter.toAllRoutineListDTO(routineList, slice);
    }

    // 타인 소비 루틴 상세 조회
    // 타인 소비 루틴 상세 조회
    @Override
    public RoutineResponse.RoutineListDetailDTO getOtherRoutineDetail(Long userId, Long routineId) {
        // 1. 루틴 조회
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.ROUTINE_NOT_FOUND));

        // 2. 루틴 금액 정보 조회
        List<RoutineAmount> routineAmounts = routineAmountRepository.findAllWithRoutineByRoutineId(routineId);

        // 3. 금액이 0원 초과인 항목만 필터링 및 DTO 변환
        List<RoutineResponse.CategoryBudgetDetailDTO> categoryBudgetList = routineAmounts.stream()
                .filter(ra -> ra.getAmount() != null && ra.getAmount() > 0)
                .map(ra -> RoutineResponse.CategoryBudgetDetailDTO.builder()
                        .categoryName(ra.getCategoryName())
                        .amount(ra.getAmount())
                        .build())
                .collect(Collectors.toList());

        // 4. 사용자 예산 정보로 적용 가능 여부 판단
        String createdMonth = LocalDate.now().withDayOfMonth(1).toString().substring(0, 7);
        Optional<Budget> myBudgetOpt = budgetRepository.findByUserIdAndCreatedMonth(userId, createdMonth);

        boolean canApply = true;
        String message = null;
        if (myBudgetOpt.isPresent() && Boolean.TRUE.equals(myBudgetOpt.get().getIsFromRoutine())) {
            canApply = false;
            message = "소비루틴은 한 달에 한 번만 반영할 수 있어요";
        }

        return RoutineResponse.RoutineListDetailDTO.builder()
                .totalBudget(routine.getRoutineTotalAmount())
                .routineName(routine.getRoutineName())
                .categoryBudgetList(categoryBudgetList)
                .canApply(canApply)
                .cannotApplyMessage(message)
                .build();
    }

    @Override
    public RoutineResponse.AllRoutineListDTO searchRoutineList(String keyword, Long cursorId) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        Slice<RoutineResponse.RoutineListDTO> slice = routineRepository.searchRoutinesByKeyword(keyword, cursorId, pageable);
        return RoutineConverter.toAllRoutineListDTO(slice.getContent(), slice);
    }

    // 타인의 소비 루틴을 내 예산에 반영 시 미리보기
    @Override
    public RoutineResponse.ApplyRoutineInfoDTO getRoutineApplyInfo(Long userId, Long routineId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 소비 루틴 조회 (자신의 루틴은 허용하지 않음)
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.ROUTINE_NOT_FOUND));

        if (routine.getUser().getId().equals(userId)) {
            throw new ErrorHandler(ErrorStatus.ROUTINE_PREVIEW_NOT_ALLOWED);
        }

        // 3. 해당 루틴의 예산 생성 월 기준으로 내 예산 조회
        Budget myBudget = budgetRepository.findByUserAndCreatedMonth(user, routine.getBudget().getCreatedMonth())
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.BUDGET_NOT_FOUND));

        if (Boolean.TRUE.equals(myBudget.getIsFromRoutine())) {
            throw new ErrorHandler(ErrorStatus.ROUTINE_ALREADY_APPLIED);
        }

        // 4. 내 예산의 소비 카테고리 로드
        List<BudgetCategory> myCategories = budgetCategoryRepository.findByBudgetWithConsumptionCategory(myBudget);
        Map<String, BudgetCategory> myCategoryMap = myCategories.stream()
                .collect(Collectors.toMap(
                        bc -> bc.getConsumptionCategory().getBudgetCategoryName(),
                        Function.identity()
                ));

        // 5. 루틴 금액 정보 조회
        List<RoutineAmount> routineAmounts = routineAmountRepository.findAllWithRoutineByRoutineId(routineId);
        Map<String, Integer> routineAmountMap = routineAmounts.stream()
                .collect(Collectors.toMap(RoutineAmount::getCategoryName, RoutineAmount::getAmount));

        // 6. Default 카테고리 처리
        Set<String> defaultNames = new HashSet<>(DefaultCategoryConstants.DEFAULT_CATEGORY_NAMES);

        List<RoutineResponse.ApplyCategoryBudgetDTO> defaultCategoryBudgets =
                DefaultCategoryConstants.DEFAULT_CATEGORY_NAMES.stream()
                        .map(name -> {
                            int amount = routineAmountMap.getOrDefault(name, 0);
                            return RoutineConverter.toCategoryDTO(name, amount, CategoryType.DEFAULT);
                        })
                        .collect(Collectors.toList());

        // 7. 루틴 기반 ROUTINE_CATEGORY 구성 (CUSTOM과 이름이 겹치는 항목 포함)
        Set<String> routineCategoryNames = new HashSet<>();
        List<RoutineResponse.ApplyCategoryBudgetDTO> routineCategoryBudgets = routineAmounts.stream()
                .filter(ra -> !defaultNames.contains(ra.getCategoryName()))
                .map(ra -> {
                    String name = ra.getCategoryName();
                    int amount = ra.getAmount();
                    routineCategoryNames.add(name);
                    return RoutineConverter.toCategoryDTO(name, amount, CategoryType.ROUTINE_CATEGORY);
                })
                .collect(Collectors.toList());

        // 8. 내 예산의 CUSTOM 항목 중 루틴에 포함되지 않은 항목만 CUSTOM으로 유지
        List<RoutineResponse.ApplyCategoryBudgetDTO> customCategoryBudgets = myCategories.stream()
                .filter(mc -> {
                    String name = mc.getConsumptionCategory().getBudgetCategoryName();
                    return mc.getConsumptionCategory().getBudgetCategoryType() == CategoryType.CUSTOM &&
                            !routineCategoryNames.contains(name);
                })
                .map(mc -> RoutineConverter.toCategoryDTO(
                        mc.getConsumptionCategory().getBudgetCategoryName(),
                        0,
                        CategoryType.CUSTOM)
                )
                .collect(Collectors.toList());

        log.info("소비 루틴 예산 반영 미리보기 완료 - userId: {}, routineId: {}", userId, routineId);

        return RoutineConverter.toApplyRoutineInfoDTO(
                routine.getRoutineTotalAmount(),
                defaultCategoryBudgets,
                customCategoryBudgets,
                routineCategoryBudgets
        );
    }

}
