package com.server.money_touch.domain.budget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

public class BudgetResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "한 달 예산 등록 응답 정보")
    public static class BudgetCreateResultDTO {
        @Schema(description = "예산 아이디", example = "1")
        private Long budgetId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "한 달 총 소비 사용 금액 응답 정보")
    public static class TotalConsumptionResultDTO {
        @Schema(description = "예산 아이디", example = "1")
        private Long budgetId;

        @Schema(description = "한 달 총 소비 금액", example = "78000")
        private Integer totalConsumption;

        @Schema(description = "한 달 예산 대비 총 소비 금액 퍼센트", example = "50")
        private Integer percent;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "한 달 예산 내역 응답 정보")
    public static class BudgetDetailDTO {

        @Schema(description = "한 달 전체 예산", example = "500000")
        private Integer totalBudget;

        @Schema(description = "기본 카테고리별 예산 목록")
        private List<BudgetResponse.BudgetDetailDTO.DefaultCategoryBudget> defaultCategoryBudgets;

        @Schema(description = "내 카테고리별 예산 목록")
        private List<BudgetResponse.BudgetDetailDTO.CustomCategoryBudget> customCategoryBudgets;

        @Schema(description = "소비 루틴 카테고리 예산 목록")
        private List<BudgetResponse.BudgetDetailDTO.RoutineCategoryBudget> routineCategoryBudgets;

        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "기본 카테고리별 예산")
        public static class DefaultCategoryBudget {
            @Schema(description = "기본 예산 카테고리명", example = "배달/외식")
            private String categoryName;

            @Schema(description = "카테고리별 예산 금액", example = "100000")
            private Integer amount;
        }

        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "사용자 정의 카테고리별 예산")
        public static class CustomCategoryBudget {
            @Schema(description = "사용자 정의 카테고리명", example = "교육비")
            private String categoryName;

            @Schema(description = "카테고리별 예산 금액", example = "150000")
            private Integer amount;
        }

        @Builder
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "소비 루틴 카테고리 예산")
        public static class RoutineCategoryBudget {
            @Schema(description = "소비 루틴 카테고리명", example = "술/유흥")
            private String categoryName;

            private Integer amount;
        }
    }
}
