package com.server.money_touch.domain.user.controller;

import com.server.money_touch.domain.user.dto.RefreshTokenRequest;
import com.server.money_touch.domain.user.dto.TokenResponse;

import com.server.money_touch.domain.user.dto.TokenValidationRequest;
import com.server.money_touch.domain.user.dto.TokenValidationResponse;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.config.jwt.TokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class TokenController {
    private final TokenProvider tokenProvider;

    /**
     * Access Token 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        TokenResponse tokenResponse = tokenProvider.refreshAccessToken(request.getRefreshToken());

        if (tokenResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.onFailure("UNAUTHORIZED", "유효하지 않은 리프레시 토큰입니다.", null));
        }

        return ResponseEntity.ok(ApiResponse.onSuccess(tokenResponse));
    }

    /**
     * 토큰 유효성 검증
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(
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

        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
