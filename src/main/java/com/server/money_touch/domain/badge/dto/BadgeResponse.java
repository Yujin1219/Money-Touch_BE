package com.server.money_touch.domain.badge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class BadgeResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "내가 획득한 배지 목록")
    public static class MyBadgeListResultDTO {

        @Schema(description = "획득한 배지 리스트")
        private List<BadgeDetailResultDTO> badges;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "배지 상세 정보")
    public static class BadgeDetailResultDTO {

        @Schema(description = "배지 ID", example = "1")
        private Long badgeId;

        @Schema(description = "배지 이름", example = "똑똑소비대장")
        private String name;

        @Schema(description = "배지 이미지 URL", example = "https://example.com/badge.png")
        private String imageUrl;

        @Schema(description = "배지 설명", example = "똑똑소비왕 10위권 달성 시")
        private String description;
    }

}
