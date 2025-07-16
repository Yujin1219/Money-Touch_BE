package com.server.money_touch.domain.user.service.user;

public interface UserQueryService {
    // User 존재 여부 검증
    Boolean existsUserById(Long userId);
}
