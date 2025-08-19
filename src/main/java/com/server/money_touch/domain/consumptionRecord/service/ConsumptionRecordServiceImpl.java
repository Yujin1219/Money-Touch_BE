package com.server.money_touch.domain.consumptionRecord.service;

import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.consumptionRecord.converter.totalConsumption.TotalConsumptionConverter;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionCategoryResponse;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordRequest;
import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecordImage;
import com.server.money_touch.domain.consumptionRecord.entity.TotalConsumption;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionCategory.ConsumptionCategoryRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordImageRepository;
import com.server.money_touch.domain.consumptionRecord.repository.consumptionRecord.ConsumptionRecordRepository;
import com.server.money_touch.domain.consumptionRecord.repository.totalConsumption.TotalConsumptionRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.GeneralException;
import com.server.money_touch.global.constants.DefaultCategoryConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsumptionRecordServiceImpl implements ConsumptionRecordService{

    private final ConsumptionRecordRepository consumptionRecordRepository;
    private final ConsumptionCategoryRepository consumptionCategoryRepository;
    private final UserRepository userRepository;
    private final TotalConsumptionRepository totalConsumptionRepository;
    private final ConsumptionRecordImageRepository consumptionRecordImageRepository;

    @Override
    @Transactional
    public ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO createConsumptionRecord(
            Long userId, ConsumptionRecordRequest.ConsumptionRecordCreateDTO request, String imageUrl){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 1. 카테고리 조회
        ConsumptionCategory category = consumptionCategoryRepository.findAllByUser(user).stream()
                .filter(cat -> cat.getBudgetCategoryName().equals(request.getCategoryName()))
                .findFirst()
                .orElseThrow(()-> new GeneralException(ErrorStatus.CONSUMPTION_CATEGORY_NOT_FOUND));

        // 2. 공개 여부 유효성
        if(Boolean.TRUE.equals(request.getIsPublic())){
            if(imageUrl == null || request.getMemo() == null){
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
                .memo(request.getMemo())
                .commentCount(0)
                .wiseCount(0)
                .wasteCount(0)
                .viewCount(0)
                .consumeDate(LocalDateTime.now())
                .build();

        consumptionRecordRepository.save(record);


        // 4. 이미지 엔티티 저장 (이미지가 있는 경우만)
        if (imageUrl != null) {
            ConsumptionRecordImage image = ConsumptionRecordImage.builder()
                    .consumptionRecord(record)
                    .name("record image")  // 필요 시 originalFilename 사용 가능
                    .filePath(imageUrl)
                    .build();
            consumptionRecordImageRepository.save(image);
        }

        return new ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO(record.getId());

    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumptionCategoryResponse.CategoryInfoDTO> getSortedCategoriesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        List<ConsumptionCategory> allCategories = consumptionCategoryRepository.findAllByUser(user);

        // 순서 고정 리스트 사용
        List<String> defaultOrder = DefaultCategoryConstants.DEFAULT_CATEGORY_NAMES;

        return allCategories.stream()
                .sorted(Comparator.comparingInt(cat -> {
                    CategoryType type = cat.getBudgetCategoryType();
                    String name = cat.getBudgetCategoryName();

                    if (type == CategoryType.DEFAULT) {
                        int index = defaultOrder.indexOf(name);
                        return index >= 0 ? index : Integer.MAX_VALUE;
                    } else if (type == CategoryType.CUSTOM) {
                        return 100; //
                    } else {
                        return 200; // ROUTINE_CATEGORY
                    }
                }))
                .map(ConsumptionCategoryResponse.CategoryInfoDTO::fromEntity)
                .collect(Collectors.toList());
    }


}