package com.server.money_touch.domain.user.service.user;

import com.server.money_touch.domain.user.dto.UserResponse;

public interface UserQueryService {
    // User 존재 여부 검증
    Boolean existsUserById(Long userId);

    // email 존재 여부 검증
    Boolean existsByEmail(String email);

    // nickname 중복 여부 검증
    Boolean existsByNickname(String nickname);

    // 마이페이지 유저 정보 조회
    UserResponse.MyPageResponseDTO getMyPageInfo(Long userId);
}
