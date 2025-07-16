package com.server.money_touch.domain.routine.dto;

import com.server.money_touch.domain.budget.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

public class RoutineRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "소비 루틴 등록 요청 정보")
    public static class RoutineCreateDTO {

        @Schema(description = "한 달 전체 예산", example = "500000")
        @NotNull(message = "전체 예산은 필수 입력 항목입니다.")
        private Integer totalBudget;

        @Schema(description = "소비 루틴 이름", example = "50만원으로 한 달 살기 루틴")
        @NotNull(message = "소비 루틴 이름은 필수입니다.")
        @Size(max = 20, message = "소비 루틴 이름은 20자 이하로 입력해주세요.")
        private String routineName;

        @Schema(description = "소비 루틴 이미지 url", example = "https://")
        String routineImgUrl;

        @Schema(description = "소비 루틴 해시태그 목록", example = "[\"#식비절약\", \"#생활비\"]")
        private List<String> hashtags;

        @Schema(description = "카테고리별 예산 목록")
        @NotNull(message = "카테고리별 예산 목록은 비어 있을 수 없습니다.")
        @Valid
        private List<CategoryBudgetDTO> categoryBudgeList;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "카테고리별 예산")
    public static class CategoryBudgetDTO {

        @Schema(description = "예산 카테고리명", example = "배달/외식")
        @NotNull(message = "카테고리 이름은 필수입니다.")
        @Size(max = 8, message = "카테고리 이름은 8자 이하로 입력해주세요.")
        private String categoryName;

        @Schema(description = "카테고리별 예산 금액", example = "100000")
        @NotNull(message = "카테고리 금액은 필수입니다.")
        private Integer amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소비 루틴 예산 반영 요청 DTO")
    public static class ApplyRoutineBudgetDTO {

        @Schema(description = "수정된 전체 예산", example = "500000")
        private Integer budgetTotal;

        @Schema(description = "반영할 카테고리 예산 목록 (기존+소비루틴 모두 합침)")
        @NotNull
        @Valid
        private List<RoutineRequest.ApplyCategoryBudgetDTO> categoryBudgets;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "카테고리별 예산 정보")
    public static class ApplyCategoryBudgetDTO {

        @Schema(description = "카테고리명", example = "배달/외식")
        private String categoryName;

        @Schema(description = "카테고리 금액", example = "100000")
        private Integer amount;

        @Schema(description = "카테고리 타입", example = "DEFAULT / CUSTOM / ROUTINE_CATEGORY")
        @NotNull
        private CategoryType categoryType;
    }
}
