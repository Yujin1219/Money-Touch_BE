package com.server.money_touch.domain.budget.dto;

import com.server.money_touch.domain.budget.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class BudgetRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "한 달 예산 등록 요청 정보")
    public static class BudgetCreateDTO {

        @Schema(description = "한 달 전체 예산", example = "350000")
        @NotNull(message = "전체 예산은 필수 입력 항목입니다.")
        private Integer totalBudget;

        @Schema(description = "기본 카테고리별 예산 목록")
        @NotNull(message = "기본 카테고리 예산 목록은 비어 있을 수 없습니다.")
        @Valid
        private List<BudgetRequest.DefaultCategoryBudget> defaultCategoryBudgets;

        @Schema(description = "내 카테고리별 예산 목록")
        @Valid
        private List<BudgetRequest.CustomCategoryBudget> customCategoryBudgets;

        @Schema(description = "소비 루틴 카테고리 예산 목록")
        @Valid
        private List<BudgetRequest.RoutineCategoryBudget> routineCategoryBudgets;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "기본 카테고리별 예산")
    public static class DefaultCategoryBudget {

        @Schema(description = "기본 예산 카테고리명", example = "배달/외식")
        @NotNull(message = "기본 카테고리 이름은 필수입니다.")
        @Size(max = 8, message = "기본 카테고리 이름은 8자 이하로 입력해주세요.")
        private String categoryName;

        @Schema(description = "카테고리별 예산 금액", example = "100000")
        @NotNull(message = "카테고리 금액은 필수입니다.")
        private Integer amount;

        @Schema(description = "카테고리 타입", example = "DEFAULT")
        private CategoryType categoryType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "사용자 정의 카테고리별 예산")
    public static class CustomCategoryBudget {

        @Schema(description = "사용자 정의 카테고리명", example = "교육비")
        @NotNull(message = "사용자 정의 카테고리 이름은 필수입니다.")
        @Size(max = 8, message = "사용자 정의 카테고리 이름은 8자 이하로 입력해주세요.")
        private String categoryName;

        @Schema(description = "카테고리별 예산 금액", example = "150000")
        @NotNull(message = "카테고리 금액은 필수입니다.")
        private Integer amount;

        @Schema(description = "카테고리 타입", example = "CUSTOM")
        private CategoryType categoryType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "소비 루틴 카테고리 예산")
    public static class RoutineCategoryBudget {

        @Schema(description = "소비 루틴 카테고리명", example = "술/유흥")
        @NotNull(message = "소비 루틴 카테고리 이름은 필수입니다.")
        @Size(max = 8, message = "소비 루틴 카테고리 이름은 8자 이하로 입력해주세요.")
        private String categoryName;

        @Schema(description = "카테고리별 예산 금액", example = "100000")
        @NotNull(message = "카테고리 금액은 필수입니다.")
        private Integer amount;

        @Schema(description = "카테고리 타입", example = "ROUTINE_CATEGORY")
        private CategoryType categoryType;
    }
}
