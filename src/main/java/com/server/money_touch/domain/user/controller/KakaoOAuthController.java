package com.server.money_touch.domain.user.controller;

import com.server.money_touch.domain.user.dto.TokenResponse;
import com.server.money_touch.global.apiPayload.ApiResponse;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExample;
import com.server.money_touch.global.validation.annotation.ApiErrorCodeExamples;
import com.server.money_touch.global.validation.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauht/kakao")
@RequiredArgsConstructor
public class KakaoOAuthController {

//    @Operation(summary = "카카오 회원가입 API", description = "Kakaokey를 사용한 소셜 회원가입 방식의 API입니다.")
//    @ApiSuccessCodeExample(resultClass = ApiSuccessCodeExample.class)
//    @ApiErrorCodeExamples({
//            @ApiErrorCodeExample(value = ErrorStatus.class, name = "USER_NOT_FOUND"),
//            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
//            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR"),
//    })
    @GetMapping("/callback")
    public ApiResponse<TokenResponse> kakaoCallback(@RequestParam("code") String code) {
        TokenResponse tokenResponse = new TokenResponse();
        return ApiResponse.onSuccess(tokenResponse);
    }


}
