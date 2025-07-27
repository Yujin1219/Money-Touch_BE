package com.server.money_touch.domain.badge.service;

import com.server.money_touch.domain.badge.converter.BadgeConverter;
import com.server.money_touch.domain.badge.dto.BadgeResponse;
import com.server.money_touch.domain.badge.entity.UserBadge;
import com.server.money_touch.domain.badge.repository.BadgeRepository;
import com.server.money_touch.domain.badge.repository.UserBadgeRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BadgeCommandServiceImpl implements BadgeCommandService {

    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;

    // 대표 배지 설정
    @Override
    public BadgeResponse.RepresentativeBadgeResultDTO setRepresentativeBadge(Long userId, Long badgeId) {
        log.info("대표 배지 설정 시작 - 사용자 ID: {}, 배지 ID: {}", userId, badgeId);

        // 1. 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 배지 존재 여부 확인
        if (!badgeRepository.existsById(badgeId)) {
            log.warn("존재하지 않는 배지 ID입니다. - badgeId: {}", badgeId);
            throw new ErrorHandler(ErrorStatus.BADGE_NOT_FOUND);
        }

        // 3. 사용자가 해당 배지를 획득했는지 확인 (UserBadge 존재 여부)
        UserBadge userBadge = userBadgeRepository.findByUserAndBadgeId(user, badgeId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.BADGE_NOT_EARNED));

        // 4. User 엔티티에 대표 배지 설정
        user.setBadgeId(badgeId);

        log.info("✅대표 배지 설정 완료 - 사용자 ID: {}, 설정된 배지 ID: {}", userId, badgeId);

        // 5. DTO 반환
        return BadgeConverter.toRepresentativeBadgeDTO(userBadge);
    }
}
