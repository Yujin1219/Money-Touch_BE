package com.server.money_touch.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class UserRequest{

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "로컬 회원가입 정보")
    public static class LocalSignUpDTO{

        @Schema(description = "닉네임", example = "잔디")
        @NotBlank(message = "닉네임은 필수입니다")
        private String nickname;

        @Schema(description = "사진 URL", example = "http://example.com/image.jpg")
        private String profileImgUrl;

        @Schema(description = "약관동의 리스트")
        private List<AgreementDTO> agreeTerms;

        @Schema(description = "이메일", example = "example@naver.com")
        @Email(message = "올바른 이메일 형식이어야합니다")
        @NotBlank(message = "이메일은 필수입니다")
        private String email;

        @Schema(description = "비밀번호", example = "12345678Abc@")
        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;
    };

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "소셜 회원가입 정보")
    public static class KakaoSignUpDto {

        @Schema(description = "카카오 ID", example = "1234567890")
        @NotBlank(message = "카카오 사용자 ID는 필수입니다.")
        private String kakaoId; // 카카오에서 받은 고유 식별자

        @Schema(description = "닉네임", example = "잔디")
        @NotBlank(message = "닉네임은 필수입니다.")
        private String nickname;

        @Schema(description = "약관동의 리스트")
        private List<AgreementDTO> agreeTerms;

        @Schema(description = "사진 URL", example = "http://example.com/image.jpg")
        private String profileImgUrl;

        // 선택: 카카오에서 이메일을 제공한 경우
        @Schema(description = "이메일", example = "example@kakao.com")
        private String email;
    }

    @Getter
    @Setter
    public static class AgreementDTO {

        @Schema(description = "약관 ID", example = "1")
        @NotNull
        private Long termsId;

        @Schema(description = "동의여부", example = "true")
        @NotNull
        private Boolean isAgree;
    }
}