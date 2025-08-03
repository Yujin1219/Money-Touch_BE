package com.server.money_touch.domain.routine.dto;

import com.server.money_touch.domain.budget.dto.BudgetRequest;
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
    @Schema(description = "소비 루틴 등록 요청 정보", example = """
{
  "totalBudget": 500000,
  "routineName": "5만원으로 한 달 살기 루틴",
  "routineImgUrl": "https://",
  "hashtags": [
    "#식비절약",
    "#생활비"
  ],
  "budgetList": [
    {
      "categoryName": "배달/외식",
      "amount": 150000
    },
    {
      "categoryName": "패션/쇼핑",
      "amount": 100000
    },
    {
      "categoryName": "교통",
      "amount": 100000
    },
    {
      "categoryName": "카페",
      "amount": 0
    },
    {
      "categoryName": "기타",
      "amount": 0
    },
    {
      "categoryName": "교육비",
      "amount": 150000
    }
  ]
}
""")
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
        private List<CategoryBudgetDTO> budgetList;
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
    @Schema(
            description = "소비 루틴 예산 반영 요청 DTO",
            example = "{\n" +
                    "  \"totalBudget\": 1000000,\n" +
                    "  \"defaultCategoryBudgets\": [\n" +
                    "    {\n" +
                    "      \"categoryName\": \"배달/외식\",\n" +
                    "      \"amount\": 150000,\n" +
                    "      \"categoryType\": \"DEFAULT\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"categoryName\": \"교통\",\n" +
                    "      \"amount\": 100000,\n" +
                    "      \"categoryType\": \"DEFAULT\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"categoryName\": \"패션/쇼핑\",\n" +
                    "      \"amount\": 100000,\n" +
                    "      \"categoryType\": \"DEFAULT\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"categoryName\": \"카페\",\n" +
                    "      \"amount\": 0,\n" +
                    "      \"categoryType\": \"DEFAULT\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"categoryName\": \"기타\",\n" +
                    "      \"amount\": 0,\n" +
                    "      \"categoryType\": \"DEFAULT\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"customCategoryBudgets\": [\n" +
                    "    {\n" +
                    "      \"categoryName\": \"데이트\",\n" +
                    "      \"amount\": 300000,\n" +
                    "      \"categoryType\": \"CUSTOM\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"routineCategoryBudgets\": [\n" +
                    "    {\n" +
                    "      \"categoryName\": \"교육비\",\n" +
                    "      \"amount\": 250000,\n" +
                    "      \"categoryType\": \"ROUTINE_CATEGORY\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"categoryName\": \"술/유흥\",\n" +
                    "      \"amount\": 100000,\n" +
                    "      \"categoryType\": \"ROUTINE_CATEGORY\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}"
    )
    public static class ApplyRoutineBudgetDTO {

        @Schema(description = "전체 예산", example = "600000")
        @NotNull(message = "전체 예산은 필수 입력 항목입니다.")
        private Integer totalBudget;

        @Schema(description = "기본 카테고리별 예산 목록")
        @NotNull(message = "기본 카테고리 예산 목록은 비어 있을 수 없습니다.")
        @Valid
        private List<ApplyCategoryBudgetDTO> defaultCategoryBudgets;

        @Schema(description = "내 카테고리별 예산 목록")
        @Valid
        private List<ApplyCategoryBudgetDTO> customCategoryBudgets;

        @Schema(description = "소비 루틴 카테고리 예산 목록")
        @Valid
        private List<ApplyCategoryBudgetDTO> routineCategoryBudgets;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "카테고리별 예산 정보")
    public static class ApplyCategoryBudgetDTO {

        @Schema(description = "카테고리명", example = "배달/외식")
        @NotNull(message = "카테고리 이름은 필수입니다.")
        private String categoryName;

        @Schema(description = "카테고리별 예산 금액", example = "100000")
        @NotNull(message = "카테고리 금액은 필수입니다.")
        private Integer amount;

        @Schema(description = "카테고리 타입", example = "DEFAULT / CUSTOM / ROUTINE_CATEGORY")
        @NotNull
        private CategoryType categoryType;
    }
}
