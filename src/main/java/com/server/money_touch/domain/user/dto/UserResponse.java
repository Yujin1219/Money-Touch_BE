package com.server.money_touch.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserResponse {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 등록 응답 정보")
    public static class UserCreateResultDTO {

        @Schema(description = "유저 id", example = "1")
        private Long userId;

        @Schema(description = "회원 생성일", example = "2021-11-08T11:44:30.327959"  )
        private LocalDateTime createdAt;
    }
}
