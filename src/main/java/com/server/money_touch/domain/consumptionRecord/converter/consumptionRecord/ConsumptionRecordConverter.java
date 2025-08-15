package com.server.money_touch.domain.consumptionRecord.converter.consumptionRecord;

import com.server.money_touch.domain.consumptionRecord.dto.ConsumptionRecordResponse;
import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionRequest;
import com.server.money_touch.domain.consumptionRecord.dto.HouseholdConsumptionResponse;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionRecord;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemDetailProjection;
import com.server.money_touch.domain.consumptionRecord.projection.DailyConsumptionItemProjection;
import com.server.money_touch.domain.fixedConsumption.entity.FixedConsumption;
import com.server.money_touch.domain.user.entity.User;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ConsumptionRecordConverter {

    // 일일 소비 기록 시 소비 카테고리 엔티티 생성
    public static ConsumptionRecord toDailyConsumptionRecord(User user, ConsumptionCategory consumptionCategory, HouseholdConsumptionRequest.DailyConsumptionCreateDTO requestDTO, Boolean isPublic) {
        return ConsumptionRecord.builder()
                .user(user)
                .consumptionCategory(consumptionCategory)
                .amount(requestDTO.getAmount())
                .content(requestDTO.getContent())
                .memo(requestDTO.getMemo())
                .consumeDate(requestDTO.getConsumeDate())
                .isPublic(isPublic) // 일일 소비 기록은 피드 없이 가계부에만 등록
                .commentCount(0)
                .wiseCount(0)
                .wasteCount(0)
                .viewCount(0)
                .build();
    }

    // 소비 기록 응답
    public static ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO toConsumptionRecordCreateResultDTO(Long consumptionRecordId){
        return ConsumptionRecordResponse.ConsumptionRecordCreateResultDTO.builder()
                .consumptionRecordId(consumptionRecordId)
                .build();
    }

    // 일일 소비 기럭 내역 조회 응답
    public static HouseholdConsumptionResponse.DailyConsumptionDetailDTO toDailyConsumptionDetailDTO(
            ConsumptionRecord record, ConsumptionCategory category) {

        String categoryName = record.getIsFixed() ? "고정비" : category.getBudgetCategoryName();

        return HouseholdConsumptionResponse.DailyConsumptionDetailDTO.builder()
                .amount(record.getAmount())
                .content(record.getContent())
                .memo(record.getMemo())
                .consumeDate(record.getConsumeDate())
                .categoryName(categoryName)
                .build();
    }

    // 소비 내역 무한스크롤 - 한달 소비 기록 조회 목록 응답 정보
    public static HouseholdConsumptionResponse.MonthlyHistoryResponseDTO toMonthlyHistoryResponseDTO(
            List<HouseholdConsumptionResponse.DailyHistoryDTO> dailyHistory,
            boolean isFirst,
            boolean hasNext,
            Long nextCursorId
    ) {
        return HouseholdConsumptionResponse.MonthlyHistoryResponseDTO.builder()
                .monthlyHistory(dailyHistory)
                .monthlyHistorySize(dailyHistory.stream().mapToInt(HouseholdConsumptionResponse.DailyHistoryDTO::getItemSize).sum())
                .isFirst(isFirst)
                .isLast(!hasNext)
                .hasNext(hasNext)
                .nextCursorId(nextCursorId)
                .build();
    }

    // 소비 내역 무한스크롤 - 날짜별 소비 기록 조회 응답 정보
    public static HouseholdConsumptionResponse.DailyHistoryDTO toDailyHistoryDTO(LocalDate date, List<HouseholdConsumptionResponse.DailyRecordDTO> records) {
        return HouseholdConsumptionResponse.DailyHistoryDTO.builder()
                .date(date.toString())
                .items(records)
                .itemSize(records.size())
                .build();
    }

    // 소비 내역 무한스크롤 - 날짜별 상세 소비 기록 조회 응답 정보
    public static HouseholdConsumptionResponse.DailyRecordDTO toDailyRecordDTO(DailyConsumptionItemProjection projection) {
        return HouseholdConsumptionResponse.DailyRecordDTO.builder()
                .consumptionRecordId(projection.getConsumptionRecordId())
                .categoryName(projection.getCategoryName())
                .content(projection.getContent())
                .amount(projection.getAmount())
                .build();
    }

    // 고정비용 소비 기록 생성
    public static ConsumptionRecord toConsumptionRecordForFix(User user, ConsumptionCategory consumptionCategory, FixedConsumption fixedConsumption, LocalDateTime startOfMonth){
        return ConsumptionRecord.builder()
                .user(user)
                .consumptionCategory(consumptionCategory)
                .amount(fixedConsumption.getFixedConsumptionAmount())
                .content(fixedConsumption.getFixedConsumptionContent())
                .memo(fixedConsumption.getFixedConsumptionMemo())
                .consumeDate(startOfMonth)
                .isPublic(true) // 가계부에만 등록
                .isFixed(true) // 고정비 여부
                .commentCount(0)
                .wiseCount(0)
                .wasteCount(0)
                .viewCount(0)
                .build();
    }

    // 달력 - 특정 날짜의 소비 내역 무한스크롤 조회 응답
    public static HouseholdConsumptionResponse.CalendarDailyConsumeSliceResponse toCalendarDailyConsumeSliceResponse(
            LocalDate targetDate,
            Slice<DailyConsumptionItemDetailProjection> slice,
            Long nextCursorId,
            boolean isFirst
    ) {
        List<HouseholdConsumptionResponse.ConsumeItemDTO> items = slice.getContent().stream()
                .map(p -> HouseholdConsumptionResponse.ConsumeItemDTO.builder()
                        .consumptionRecordId(p.getConsumptionRecordId())
                        .categoryName(p.getCategoryName())
                        .content(p.getContent())
                        .amount(p.getAmount())
                        .build())
                .toList();

        return HouseholdConsumptionResponse.CalendarDailyConsumeSliceResponse.builder()
                .date(targetDate.toString())
                .items(items)
                .itemSize(items.size())
                .isFirst(isFirst)
                .isLast(!slice.hasNext())
                .hasNext(slice.hasNext())
                .nextCursorId(nextCursorId)
                .build();
    }
}
