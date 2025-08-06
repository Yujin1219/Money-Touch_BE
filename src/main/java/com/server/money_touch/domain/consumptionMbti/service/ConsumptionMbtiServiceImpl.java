package com.server.money_touch.domain.consumptionMbti.service;

import com.server.money_touch.domain.consumptionMbti.converter.ConsumptionMbtiConveter;
import com.server.money_touch.domain.consumptionMbti.dto.ConsumptionMbtiResponse;
import com.server.money_touch.domain.consumptionMbti.entity.ConsumptionMbti;
import com.server.money_touch.domain.consumptionMbti.repository.ConsumptionMbtiRepository;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import com.server.money_touch.global.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConsumptionMbtiServiceImpl implements ConsumptionMbtiService{

    private final ConsumptionMbtiRepository consumptionMbtiRepository;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;

    // 소비 MBTI 존재 여부 검증
    @Override
    public Boolean existsConsumptionMbti(Long ConsumptionMbtiId) {
        return consumptionMbtiRepository.existsById(ConsumptionMbtiId);
    }

    // 유저 프로필이미지 존재 여부 검증


    @Override
    public ConsumptionMbtiResponse.ConsumptionMbtiResultDTO getConsumptionMbti(String result, HttpServletRequest request) {
        // 1. MBTI 결과 조회
        ConsumptionMbti consumptionMbti = consumptionMbtiRepository.findByResult(result)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.MBTI_NOT_FOUND));

        // 2. 현재 로그인한 유저 ID 추출
        Long userId = authUtil.getUserIdFromRequest(request);

        // 3. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorHandler(ErrorStatus.USER_NOT_FOUND));

        // 유저의 프로필 이미지가 없고, 소비 MBTI 이미지가 존재할 경우
        if ((user.getProfileImgUrl() == null || user.getProfileImgUrl().isBlank())
                && consumptionMbti.getMbtiImgUrl() != null && !consumptionMbti.getMbtiImgUrl().isBlank()) {
            user.setProfileImgUrl(consumptionMbti.getMbtiImgUrl());
            userRepository.save(user); // 유저 정보 저장
        }

        return ConsumptionMbtiConveter.toResultDTO(consumptionMbti);
    }

}
