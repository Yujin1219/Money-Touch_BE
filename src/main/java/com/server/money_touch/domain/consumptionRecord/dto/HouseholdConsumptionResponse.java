package com.server.money_touch.domain.consumptionRecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class HouseholdConsumptionResponse {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "일일 소비 기록 조회 응답 정보")
    public static class DailyConsumptionDetailDTO {
        @Schema(description = "카테고리 이름", example = "배달/외식")
        private String categoryName;

        @Schema(description = "비용", example = "12000")
        private Integer amount;

        @Schema(description = "항목명", example = "신라방 마라탕")
        private String content;

        @Schema(description = "메모" , example = "마라탕 맛있었다.")
        private String memo;

        @Schema(description = "소비 기록 날짜", example = "2022-03-31T00:00:00", type = "string")
        private LocalDateTime consumeDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "한달 소비 기록 조회 목록 응답 정보", example = """
{
  "dailyHistory": [
    {
      "date": "2025-07-02",
      "items": [
        {
          "consumptionRecordId": 3,
          "categoryName": "카페",
          "content": "스타벅스",
          "amount": 12000
        },
        {
          "consumptionRecordId": 2,
          "categoryName": "편의점",
          "content": "CU",
          "amount": 4500
        }
      ],
      "itemsSize": 2
    },
    {
      "date": "2025-07-01",
      "items": [
        {
          "consumptionRecordId": 1,
          "categoryName": "교통",
          "content": "버스",
          "amount": 1350
        }
      ],
      "itemsSize": 1
    }
  ],
  "dailyHistorySize": 3,
  "isFirst": true,
  "isLast": false,
  "hasNext": false
}
""")
    public static class MonthlyHistoryResponseDTO {

        @Schema(description = "한달 소비 내역 목록")
        private List<HouseholdConsumptionResponse.DailyHistoryDTO> monthlyHistory;

        @Schema(description = "한달 소비 내역 총 개수", example = "25")
        private Integer monthlyHistorySize;

        @Schema(description = "첫 페이지 여부", example = "true")
        private Boolean isFirst;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private Boolean isLast;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private Boolean hasNext;

        @Schema(description = "다음 요청에 사용할 커서 (마지막 소비 기록 ID)", example = "3")
        private Long nextCursorId;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "날짜별 소비 기록 조회 응답 정보")
    public static class DailyHistoryDTO {

        @Schema(description = "날짜", example = "2025-07-02")
        private String date;

        @Schema(description = "해당 날짜의 일일 소비 내역 목록")
        private List<HouseholdConsumptionResponse.DailyRecordDTO> items;

        @Schema(description = "해당 날짜의 일일 소비 내역 총 개수", example = "4")
        private Integer itemSize;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "날짜별 상세 소비 기록 조회 응답 정보")
    public static class DailyRecordDTO {

        @Schema(description = "소비 기록 아이디", example = "1")
        private Long consumptionRecordId;

        @Schema(description = "카테고리 이름", example = "배달/외식")
        private String categoryName;

        @Schema(description = "항목명", example = "스타벅스")
        private String content;

        @Schema(description = "소비 금액", example = "12000")
        private Integer amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "달력 한 달 소비 기록 조회 응답 정보")
    public static class CalendarDateAmountMapDTO {

        @Schema(description = "날짜별 사용 금액 맵",
                example = """
            {
              "2025-07-02": 53000,
              "2025-07-03": 12000,
              "2025-07-05": 5000
            }
        """,
                implementation = Map.class
        )
        private Map<String, Integer> data;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "달력 특정 날짜의 소비 상세 내역", example = """
{
  "date": "2025-07-02",
  "items": [
    {
      "consumptionRecordId": 1,
      "categoryName": "카페",
      "content": "스타벅스",
      "amount": 5000
    },
    {
      "consumptionRecordId": 2,
      "categoryName": "편의점",
      "content": "CU",
      "amount": 3500
    },
    {
      "consumptionRecordId": 3,
      "categoryName": "교통",
      "content": "버스",
      "amount": 7000
    }
  ],
  "itemSize": 3
}
""")    public static  class CalendarDailyConsumeDetailDTO {

        @Schema(description = "소비 날짜", example = "2025-07-02")
        private String date;

        @Schema(description = "일일 소비 항목 목록")
        private List<HouseholdConsumptionResponse.ConsumeItemDTO> items;

        @Schema(description = "일일 소비 항목 개수", example = "3")
        private Integer itemSize;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "소비 항목")
    public static class ConsumeItemDTO {

        @Schema(description = "소비 기록 아이디", example = "1")
        private Long consumptionRecordId;

        @Schema(description = "카테고리 이름", example = "배달/외식")
        private String categoryName;

        @Schema(description = "항목명", example = "스타벅스")
        private String content;

        @Schema(description = "소비 금액", example = "5000")
        private Integer amount;
    }
}
