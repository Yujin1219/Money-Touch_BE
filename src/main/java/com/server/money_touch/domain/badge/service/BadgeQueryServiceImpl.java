package com.server.money_touch.domain.badge.service;

import com.server.money_touch.domain.badge.converter.BadgeConverter;
import com.server.money_touch.domain.badge.dto.BadgeResponse;
import com.server.money_touch.domain.badge.entity.UserBadge;
import com.server.money_touch.domain.badge.repository.UserBadgeRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BadgeQueryServiceImpl implements BadgeQueryService {

    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    // 내가 획득한 배지 목록 조회
    @Override
    public BadgeResponse.MyBadgeListResultDTO getMyBadges(Long userId) {

        // 1. 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 사용자가 획득한 배지 목록 조회
        List<UserBadge> userBadges = userBadgeRepository.findByUser(user);

        log.info("사용자 배지 조회 완료 - 사용자 ID: {}, 배지 개수: {}", userId, userBadges.size());

        // 3. DTO 반환
        return BadgeConverter.toMyBadgeListDTO(userBadges);
    }


    // 현재 대표 배지 조회
    @Override
    public BadgeResponse.RepresentativeBadgeResultDTO getRepresentativeBadge(Long userId) {

        log.info("대표 배지 조회 시작 - 사용자 ID: {}", userId);

        // 1. 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 대표 배지가 설정되어있는지 확인 -> 대표배지가 없는 경우 null
        Long badgeId = user.getBadgeId();
        if(badgeId == null) {
            log.info("사용자의 대표 배지가 설정되어있지 않음 - 사용자 ID: {}", userId);
            return null;
        }

        // 3. 사용자가 해당 배지를 획득했는지 확인
        UserBadge userBadge = userBadgeRepository.findByUserAndBadgeId(user, badgeId)
                .orElseThrow(() -> {
                    log.warn("사용자가 획득하지 않은 배지가 대표 배지로 설정됨 - 사용자 ID: {}, 배지 ID: {}", userId, badgeId);
                    return new ErrorHandler(ErrorStatus.BADGE_NOT_EARNED);
                });

        log.info("✅현재 대표 배지 조회 완료 - 사용자 ID: {}, 배지 ID: {}", userId, badgeId);

        // 4. DTO 반환
        return BadgeConverter.toRepresentativeBadgeDTO(userBadge);
    }
}
