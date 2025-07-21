package com.server.money_touch.domain.consumptionRecord.service;

import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordRequest;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsumptionRecordServiceImpl implements ConsumptionRecordService{

    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final ConsumptionCategoryRepository consumptionCategoryRepository;
    private final UserRepository userRepository;

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
                .build();

        consumptionRecordRepository.save(record);

        return new ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO(record.getId());


    }



}
