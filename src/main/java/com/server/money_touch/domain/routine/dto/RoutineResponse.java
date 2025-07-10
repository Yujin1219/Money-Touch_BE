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
        List<MyRoutineDetailDTO> routineList;

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
    @Schema(description = "내가 등록한 소비 루틴 세부 정보, 소비 루틴 조회시 List로 전달")
    public static class MyRoutineDetailDTO {

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
    @AllArgsConstructor
    @Schema(description = "전체 소비 루틴 리스트")
    public static class RoutineListDTO {

        @Schema(description = "소비 루틴 아이디", example = "1")
        private Long routineId;

        @Schema(description = "NEW 여부", example = "true")
        private boolean isNew;

        @Schema(description = "소비 루틴 등록 날짜", example = "2025-07-09")
        private String createDate;

        @Schema(description = "소비 루틴 이름", example = "50만원으로 한 달 살기 루틴")
        private String routineName;

        @Schema(description = "닉네임", example = "라인")
        private String nickname;

        @Schema(description = "소비 루틴 이미지 url", example = "https://")
        private String routineImgUrl;

        @Schema(description = "프로필 이미지 url", example = "https://")
        private String profileImgUrl;

        @Schema(description = "소비 루틴 해시태그 목록", example = "[\"#식비절약\", \"#생활비\"]")
        private List<String> hashtags;

    }


}
