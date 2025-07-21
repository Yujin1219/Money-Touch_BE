package com.server.money_touch.domain.consumptionRecord.dto;

import com.server.money_touch.domain.consumptionRecord.entity.ConsumptionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ConsumptionCategoryResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소비 카테고리 정보 DTO")
    public static class CategoryInfoDTO {

        @Schema(description = "카테고리 이름", example = "배달/외식")
        private String categoryName;

        public static CategoryInfoDTO fromEntity(ConsumptionCategory category) {
            return new CategoryInfoDTO(
                    category.getBudgetCategoryName()
            );
        }
    }

}
