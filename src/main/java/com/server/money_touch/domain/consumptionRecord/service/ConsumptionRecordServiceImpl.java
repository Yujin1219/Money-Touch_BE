package com.server.money_touch.domain.consumptionRecord.service;

import com.server.money_touch.domain.consumptionRecord.converter.totalConsumption.TotalConsumptionConverter;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordRequest;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.TotalConsumption;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.consumptionRecord.repository.totalConsumption.TotalConsumptionRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsumptionRecordServiceImpl implements ConsumptionRecordService{

    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final ConsumptionCategoryRepository consumptionCategoryRepository;
    private final UserRepository userRepository;
    private final TotalConsumptionRepository totalConsumptionRepository;

    @Override
    @Transactional
    public ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO createConsumptionRecord(Long userId, ConsumptionRecordRequest.ConsumptionRecordCreateDTO request){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 1. 카테고리 조회
        ConsumptionCategory category = consumptionCategoryRepository.findAllByUser(user).stream()
                .filter(cat -> cat.getBudgetCategoryName().equals(request.getCategoryName()))
                .findFirst()
                .orElseThrow(()-> new GeneralException(ErrorStatus.CONSUMPTION_CATEGORY_NOT_FOUND));

        // 2. 공개 여부 유효성
        if(Boolean.TRUE.equals(request.getIsPublic())){
            if(request.getImageUrl() == null || request.getMemo() == null){
                throw new GeneralException(ErrorStatus._BAD_REQUEST);
            }
        }

        // 3. 소비기록 생성
        ConsumptionRecord record = ConsumptionRecord.builder()
                .user(user)
                .consumptionCategory(category)
                .amount(request.getAmount())
                .content(request.getContent())
                .isPublic(request.getIsPublic() != null && request.getIsPublic())
                .imageUrl(request.getImageUrl())
                .memo(request.getMemo())
                .commentCount(0)
                .wiseCount(0)
                .wasteCount(0)
                .viewCount(0)
                .build();

        consumptionRecordRepository.save(record);

        // 4-1. 현재 연도와 월 기준으로 월 시작일과 종료일 계산
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        // 4-2. 해당 월의 총 소비 금액 조회, 데이터가 없다면 생성
        TotalConsumption totalConsumption = totalConsumptionRepository
                .findByUserAndCreatedAtBetween(user, startOfMonth, endOfMonth)
                .orElseGet(() -> totalConsumptionRepository.save(TotalConsumptionConverter.toTotalConsumption(user)));

        // 4-3. 소비 금액 추가
        totalConsumption.updateAddTotalConsumptionAmount(request.getAmount());

        return new ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO(record.getId());


    }



}
