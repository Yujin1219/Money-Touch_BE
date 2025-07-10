package com.server.money_touch.domain.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class HomeResponse {

    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(description = "소비 통계 응답 정보")
    public static class ConsumptionStatisticsDTO {

        @Schema(description = "카테고리 이름", example = "배달/외식")
        private String categoryName;

        @Schema(description = "소비 비율(소수점 1자리까지. 합계 100%)", example = "35")
        private double percentage;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "소비 통계 Top5 카테고리 + 기타")
    public static class ConsumptionStatisticsTopResponseDTO {

        @Schema(description = "상위 5개 소비 카테고리")
        private List<ConsumptionStatisticsDTO> topCategories;

        @Schema(description = "그 외 카테고리 존재 여부", example = "true")
        private boolean hasOthers;

        @Schema(description = "이번 달 최다 소비 항목", example = "배달/외식")
        private String mostSpentCategoryName;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "기타 카테고리 상세")
    public static class OtherCategoryStatisticsResponseDTO {
        private List<ConsumptionStatisticsDTO> otherCategories;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "소비왕 랭킹 응답 DTO")
    public static class WiseRankingResponseDTO {

        @Schema(description = "상위 10명의 유저 정보")
        private List<RankingUserDTO> top10Users;

        @Schema(description = "본인 순위 정보")
        private MyRankingDTO myRank;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "소비왕 랭킹 유저 10명")
    public static class RankingUserDTO {

        @Schema(description = "닉네임", example = "제이")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://")
        private String profileImgUrl;

        @Schema(description = "현명해요 수", example = "100")
        private int wiseCount;

        @Schema(description = "등락 상태 (UP, DOWN, SAME)", example = "UP")
        private String rankChangeStatus;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "본인 순위 정보")
    public static class MyRankingDTO {
        @Schema(description = "닉네임", example = "라인")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://")
        private String profileImgUrl;

        @Schema(description = "랭킹", example = "89")
        private int ranking;

        @Schema(description = "총 현명해요 수", example = "11")
        private int totalWiseCount;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "소비 루틴 조회순 5개")
    public static class RoutinePreviewDTO {

        @Schema(description = "소비 루틴 id", example = "1")
        private Long routineId;

        @Schema(description = "아이콘 이미지", example = "https://")
        private String iconImgUrl;

        @Schema(description = "루틴 이름", example = "50만원으로 한 달 살기 루틴")
        private String routineName;

        @Schema(description = "당일 등록 여부", example = "true")
        private boolean isNew;
    }


}