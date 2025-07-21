package com.server.money_touch.domain.user.service.user;

import com.server.money_touch.domain.user.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    // User 존재 여부 검증
    @Override
    public Boolean existsUserById(Long userId) {
        return userRepository.findById(userId).isPresent();
    }
}