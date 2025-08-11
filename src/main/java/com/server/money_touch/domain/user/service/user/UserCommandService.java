package com.server.money_touch.domain.user.service.user;

import com.server.money_touch.domain.user.dto.UserRequest;
import com.server.money_touch.domain.user.dto.UserResponse;

public interface UserCommandService {

    // 로컬 회원가입
    UserResponse.UserCreateResultDTO signUpLocal(UserRequest.LocalSignUpDTO request);
    // 로컬 로그인
    UserResponse.LoginResultDTO loginLocal(UserRequest.LocalLoginDTO request);
    // 토큰 갱신
    UserResponse.LoginResultDTO refreshToken(String refreshToken);
    // 유저 상세정보 등록
    UserResponse.UserDetailCreateResultDTO saveUserDetails(Long userId, UserRequest.UserDetailCreateDTO request);
    // 회원 삭제
    UserResponse.UserDeleteResultDTO deleteUser(Long userId);



}