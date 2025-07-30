package com.server.money_touch.domain.user.controller;

import com.server.money_touch.domain.user.converter.UserConverter;
import com.server.money_touch.domain.user.dto.UserResponse;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.service.user.AuthService;
import com.server.money_touch.global.apiPayload.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class AuthController {

    private final AuthService authService;


    @GetMapping("/auth/login/kakao")
    public ApiResponse<UserResponse.UserCreateResultDTO> kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse httpServletResponse) {
        User user = authService.oAuthLogin(accessCode, httpServletResponse);
        return ApiResponse.onSuccess(UserConverter.toUserCreateResultDTO(user));
    }
}
