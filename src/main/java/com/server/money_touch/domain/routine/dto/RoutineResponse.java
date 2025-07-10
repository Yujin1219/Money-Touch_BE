package com.server.money_touch.domain.routine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class RoutineResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소비 루틴 등록 응답 정보")
    public static class RoutineCreateResultDTO {
        @Schema(description = "소비 루틴 아이디", example = "1")
        private Long routineId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "가계부 내가 등록한 소비 루틴 정보")
    public static class MyRoutineListDTO {
        @Schema(description = "내가 등록한 소비 루틴 목록")
        List<RoutineThumbnailDTO> routineList;

        @Schema(description = "현재 페이지의 소비 루틴 개수", example = "20")
        Integer routineListSize;

        @Schema(description = "페이지 처음 여부", example = "true")
        Boolean isFirst;

        @Schema(description = "페이지 마지막 여부", example = "false")
        Boolean isLast;

        @Schema(description = "다음 페이지가 있는지 여부", example = "true")
        Boolean hasNext;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "내가 등록한 소비 루틴 썸네일 정보, 소비 루틴 조회시 List로 전달")
    public static class RoutineThumbnailDTO {

        @Schema(description = "소비 루틴 아이디", example = "1")
        private Long routineId;

        @Schema(description = "소비 루틴 등록 날짜", example = "2025-07-05T13:45:30\n")
        private LocalDateTime createDate;

        @Schema(description = "소비 루틴 이름", example = "50만원으로 한 달 살기 루틴")
        private String routineName;

        @Schema(description = "닉네임", example = "라인")
        String nickname;

        @Schema(description = "소비 루틴 이미지 url", example = "https://")
        String routineImgUrl;

        @Schema(description = "프로필 이미지 url", example = "https://")
        String profileImgUrl;

        @Schema(description = "소비 루틴 해시태그 목록", example = "[\"#식비절약\", \"#생활비\"]")
        private List<String> hashtags;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "나의 소비 루틴 상세 응답 정보")
    public static class RoutineDetailDTO {

        @Schema(description = "한 달 전체 예산", example = "500000")
        private Integer totalBudget;

        @Schema(description = "소비 루틴 이름", example = "50만원으로 한 달 살기 루틴")
        private String routineName;

        @Schema(description = "소비 루틴 소개", example = "적당히 놀고 쓸만큼 써도 50만원이면 해결할 수 있어요!")
        private String routineContent;

        @Schema(description = "카테고리별 예산 목록", example = """
        [
          {
            "categoryName": "배달/외식",
            "amount": 100000
          },
          {
            "categoryName": "카페",
            "amount": 400000
          }
        ]
        """)
        private List<RoutineResponse.CategoryBudgetDetailDTO> categoryBudgetList;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "카테고리별 예산 정보")
    public static class CategoryBudgetDetailDTO {

        @Schema(description = "예산 카테고리명", example = "배달/외식")
        private String categoryName;

        @Schema(description = "카테고리별 예산 금액", example = "100000")
        private Integer amount;
    }
}
