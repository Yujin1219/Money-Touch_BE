package com.server.money_touch.domain.user.controller;

import com.server.money_touch.domain.user.dto.RefreshTokenRequest;
import com.server.money_touch.domain.user.dto.TokenResponse;

import com.server.money_touch.domain.user.dto.TokenValidationRequest;
import com.server.money_touch.domain.user.dto.TokenValidationResponse;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.config.jwt.TokenProvider;

import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Tag(name = "Refresh Token & Access Token", description = "토큰에 관한 API")
@RestController
@Validated
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class TokenController {
    private final TokenProvider tokenProvider;

    /**
     * Access Token 재발급
     */
    @Operation(summary = "Access Token 재발급 API",
    description = "Refresh Token을 이용하여 Access Token을 재발급하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOKEN_INVALID_SIGNATURE"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOKEN_EXPIRED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = tokenProvider.refreshAccessToken(request.getRefreshToken());
        if (tokenResponse == null) {
            return ApiResponse.onFailure("TOKEN4002", "유효하지 않은 리프레시 토큰입니다.", null);
        }

        return ApiResponse.onSuccess(tokenResponse);
    }

    /**
     * 토큰 유효성 검증
     */
    @Operation(summary = "토큰 유효성 검증 API",
            description = "토큰에 대해 유효성을 검증하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOKEN_INVALID_SIGNATURE"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "TOKEN_EXPIRED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    @PostMapping("/validate")
    public ApiResponse<TokenValidationResponse> validateToken(
            @RequestBody TokenValidationRequest request) {

        boolean isValid = tokenProvider.validateToken(request.getToken());
        Date expiration = null;

        if (isValid) {
            try {
                expiration = tokenProvider.getExpirationFromToken(request.getToken());
            } catch (Exception e) {
                log.warn("토큰 만료 시간 추출 실패: {}", e.getMessage());
            }
        }

        TokenValidationResponse response = TokenValidationResponse.builder()
                .valid(isValid)
                .expiration(expiration)
                .build();
        return ApiResponse.onSuccess(response);
    }
}
