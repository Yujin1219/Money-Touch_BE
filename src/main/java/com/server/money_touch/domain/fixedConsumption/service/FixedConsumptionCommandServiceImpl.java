package com.server.money_touch.domain.fixedConsumption.service;

import com.server.money_touch.domain.budget.converter.budgetCategory.BudgetCategoryConverter;
import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.budget.repository.budget.BudgetRepository;
import com.server.money_touch.domain.budget.repository.budgetCategory.BudgetCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.converter.consumptionRecord.ConsumptionRecordConverter;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.fixedConsumption.converter.FixedConsumptionConverter;
import com.server.money_touch.domain.fixedConsumption.dto.FixedConsumptionRequest;
import com.server.money_touch.domain.fixedConsumption.dto.FixedConsumptionResponse;
import com.server.money_touch.domain.fixedConsumption.entity.FixedConsumption;
import com.server.money_touch.domain.fixedConsumption.repository.FixedConsumptionRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FixedConsumptionCommandServiceImpl implements FixedConsumptionCommandService {
    private final FixedConsumptionRepository fixedConsumptionRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final ConsumptionCategoryRepository consumptionCategoryRepository;
    private final ConsumptionRecordRepository consumptionRecordRepository;

    /**
     * ✅ 고정비 등록
     * - 사용자가 새 고정비를 등록하면 즉시 DB에 저장
     * - 등록 즉시 이번 달 소비내역 및 예산에도 반영
     * - appliedThisMonth 플래그로 중복 반영 방지
     */
    @Transactional
    @Override
    public FixedConsumptionResponse.FixedConsumptionCreateResultDTO saveFixedConsumption(
            Long userId, FixedConsumptionRequest.FixedConsumptionCreateDTO request) {

        // 1) 카테고리 이름 유효성 검사 (고정비는 기본 소비 카테고리만 허용)
        if (!DefaultCategoryConstants.DEFAULT_CATEGORY_NAMES.contains(request.getCategoryName())) {
            throw new ErrorHandler(ErrorStatus.CONSUMPTION_CATEGORY_NAME_NOT_FOUND);
        }

        // 2) 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 3) 고정비 생성 및 저장
        FixedConsumption fixedConsumption = FixedConsumptionConverter.toFixedConsumption(user, request);
        fixedConsumptionRepository.save(fixedConsumption);

        // 4) 기본 ConsumptionCategory 매핑 (카테고리명 → 엔티티)
        Map<String, ConsumptionCategory> categoryMap = consumptionCategoryRepository
                .findAllByUserAndBudgetCategoryType(user, CategoryType.DEFAULT)
                .stream()
                .collect(Collectors.toMap(ConsumptionCategory::getBudgetCategoryName, c -> c));

        String categoryName = fixedConsumption.getCategoryName();
        ConsumptionCategory category = categoryMap.get(categoryName);
        if (category == null) {
            // 기본 카테고리 보정이 실패했거나 이름이 불일치하는 경우
            log.warn("❌ 고정비 카테고리 매핑 실패 - userId: {}, categoryName: {}", user.getId(), categoryName);
            return FixedConsumptionConverter.toFixedConsumptionCreateResultDTO(fixedConsumption.getId());
        }

        // 5) 소비 기록 생성 및 저장 (이번 달 1일 00:00 기준)
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay(); // 이번 달 시작일/예산 식별용 문자열(YYYY-MM)
        ConsumptionRecord record = ConsumptionRecordConverter
                .toConsumptionRecordForFix(user, category, fixedConsumption, startOfMonth);
        consumptionRecordRepository.save(record);

        // 6) flag 업데이트 (이번 달 반영 완료)
        fixedConsumption.markAsApplied(); // 엔티티 내에서 appliedThisMonth = true
        fixedConsumptionRepository.save(fixedConsumption);

        log.info("✅ 고정비 등록 완료 - userId: {}, fixedConsumptionId: {}",
                userId, fixedConsumption.getId());

        return FixedConsumptionConverter.toFixedConsumptionCreateResultDTO(fixedConsumption.getId());
    }

    // 고정비 수정
    @Transactional
    @Override
    public void updateFixedConsumption(
            Long userId, Long fixedConsumptionId, FixedConsumptionRequest.FixedConsumptionCreateDTO request) {

        // 카테고리 이름 유효성 검사 - 고정비는 기본 소비 카테고리만 등록 가능
        if (!DefaultCategoryConstants.DEFAULT_CATEGORY_NAMES.contains(request.getCategoryName())) {
            throw new ErrorHandler(ErrorStatus.CONSUMPTION_CATEGORY_NAME_NOT_FOUND);
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 기존 고정비 조회 및 사용자 일치 확인
        FixedConsumption fixedConsumption = fixedConsumptionRepository.findByIdAndUserId(fixedConsumptionId, userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.FIXED_CONSUMPTION_NOT_FOUND));

        // 고정비 정보 수정
        fixedConsumption.updateInfo(
                request.getAmount(),
                request.getContent(),
                request.getMemo(),
                request.getCategoryName()
        );

        log.info("고정비 수정 완료 - userId: {}, fixedConsumptionId: {}", userId, fixedConsumption.getId());
    }

    // 고정비 삭제
    @Transactional
    @Override
    public void deleteFixedConsumption(Long userId, Long fixedConsumptionId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 고정비 존재 여부 및 사용자 일치 확인
        FixedConsumption fixedConsumption = fixedConsumptionRepository.findByIdAndUserId(fixedConsumptionId, userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.FIXED_CONSUMPTION_NOT_FOUND));

        fixedConsumptionRepository.delete(fixedConsumption);
        log.info("고정비 삭제 완료 - userId: {}, fixedConsumptionId: {}", userId, fixedConsumptionId);
    }

    // 고정비 수동 갱신
    /**
     * ✅ 고정비 수동 갱신 (관리자 수동 실행/즉시 실행용)
     * - 모든 유저의 고정비를 이번 달 소비내역 + 예산에 반영
     * - 이미 이번 달에 반영된 고정비는 SKIP
     */
    @Override
    @Transactional
    public void postFixedConsumptionsByManual() {
        log.info("🛠 [수동 실행] 고정비 소비 기록 반영 시작");

        // 1) 이번 달 시작일 (1일 00:00)
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // 2) 모든 유저 조회
        var users = userRepository.findAllWithUserDetail();

        // 3) 각 유저 단위 처리
        users.forEach(user -> {
            log.info("▶️ [수동 실행] 사용자 {} 고정비 반영 시작", user.getId());

            // (a) 기본 ConsumptionCategory 매핑 (카테고리명 → 엔티티)
            Map<String, ConsumptionCategory> categoryMap = consumptionCategoryRepository
                    .findAllByUserAndBudgetCategoryType(user, CategoryType.DEFAULT)
                    .stream()
                    .collect(Collectors.toMap(ConsumptionCategory::getBudgetCategoryName, c -> c));

            // (b) 고정비 순회
            fixedConsumptionRepository.findAllByUser(user).forEach(fc -> {
                String categoryName = fc.getCategoryName();
                ConsumptionCategory category = categoryMap.get(categoryName);

                if (category == null) {
                    log.warn("❌ 고정비 카테고리 매핑 실패 - userId: {}, categoryName: {}", user.getId(), categoryName);
                    return;
                }

                // ✅ 이번 달 이미 반영된 고정비라면 SKIP
                if (Boolean.TRUE.equals(fc.getAppliedThisMonth())) {
                    log.info("⚠️ 이미 이번 달 반영된 고정비 - userId={}, content={}", user.getId(), fc.getFixedConsumptionContent());
                    return;
                }

                // (1) 소비 기록 생성 및 저장
                ConsumptionRecord record = ConsumptionRecordConverter.toConsumptionRecordForFix(user, category, fc, startOfMonth);
                consumptionRecordRepository.save(record);

                // (2) flag 업데이트
                fc.markAsApplied();
                fixedConsumptionRepository.save(fc);
            });

            log.info("✅ [수동 실행] 사용자 {}", user.getId());
        });

        log.info("✅ [수동 실행] 전체 사용자 고정비 소비 기록 및 예산 반영 완료 (userCount={})", users.size());
    }

}
