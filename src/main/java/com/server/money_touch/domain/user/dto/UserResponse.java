package com.server.money_touch.domain.user.dto;
import com.server.money_touch.domain.badge.entity.Badge;
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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "유저 상세 정보 등록 응답 정보")
    public static class UserDetailCreateResultDTO{

        @Schema(description = "유저 ID", example = "1")
        private Long userId = 1L;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그인 응답 정보")
    public static class LoginResultDTO {
        private String accessToken;
        private String refreshToken;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "마이페이지 프로필 정보")
    public static class MyPageResponseDTO {
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "닉네임", example = "라인")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImgUrl;

        @Schema(description = "대표 뱃지 ID", example = "1")
        private Badge badgeId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "대표 배지 설정 응답")
    public static class RepresentativeBadgeResultDTO {

        @Schema(description = "설정된 대표 배지 ID", example = "1")
        private Long badgeId;

        @Schema(description = "배지 이름", example = "절약왕")
        private String badgeName;

        @Schema(description = "배지 이미지 URL", example = "https://example.com/badge.png")
        private String badgeImageUrl;

        @Schema(description = "배지 설명", example = "똑똑소비왕 10위권 달성 시")
        private String badgeDescription;

    }
}
