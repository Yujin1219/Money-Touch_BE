package com.server.money_touch.domain.user.service.user;

import com.server.money_touch.domain.badge.entity.Badge;
import com.server.money_touch.domain.badge.entity.UserBadge;
import com.server.money_touch.domain.badge.repository.UserBadgeRepository;
import com.server.money_touch.domain.user.converter.UserConverter;
import com.server.money_touch.domain.user.dto.UserResponse;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import com.server.money_touch.global.utils.SendGridUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Slf4j
public class UserQueryServiceImpl implements UserQueryService {

    private final SendGridUtil sendGridUtil;
    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;

    // User 존재 여부 검증
    @Override
    public Boolean existsUserById(Long userId) {
        return userRepository.findById(userId).isPresent();
    }

    // 이메일 존재 여부 검증
    @Override
    public Boolean existsByEmail(String email){return userRepository.findByEmail(email).isPresent();}

    // 닉네임 중복 존재 여부 검증
    @Override
    public Boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // 이메일 인증번호 발송
    public void requestEmailVerification(String toEmail, boolean isResend) throws IOException {

        // 이메일 중복 체크
        if (existsByEmail(toEmail)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 이메일 발송
        sendGridUtil.sendEmail(toEmail, isResend);
    }

    // 마이페이지
    @Override
    public UserResponse.MyPageResponseDTO getMyPageInfo(Long userId) {

        // 1. 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 2. 사용자가 대표 배지를 설정한 경우에만 조회
        Badge badge = null;
        if (user.getBadgeId() != null) {
            // 2-1. 사용자가 해당 배지를 획득했는지 확인
            UserBadge userBadge = userBadgeRepository.findByUserAndBadgeId(user, user.getBadgeId())
                    .orElseThrow(() -> new ErrorHandler(ErrorStatus.BADGE_NOT_EARNED));

            // 2-2. 배지 정보 추출
            badge = userBadge.getBadge();
        }

        // 3. 응답 DTO 변환 및 반환
        return UserConverter.toMyPageResponseDTO(user, badge);
    }
}