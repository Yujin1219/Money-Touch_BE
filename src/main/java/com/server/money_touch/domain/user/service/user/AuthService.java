package com.server.money_touch.domain.user.service.user;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.budget.service.budget.BudgetCommandService;
import com.server.money_touch.domain.consumptionRecord.converter.totalConsumption.TotalConsumptionConverter;
import com.server.money_touch.domain.consumptionRecord.repository.totalConsumption.TotalConsumptionRepository;
import com.server.money_touch.domain.user.dto.KakaoDTO;
import com.server.money_touch.domain.user.dto.TokenResponse;
import com.server.money_touch.domain.user.dto.UserResponse;
import com.server.money_touch.domain.user.entity.CustomUserDetails;
import com.server.money_touch.domain.user.entity.SocialLogin;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.enums.AuthType;
import com.server.money_touch.domain.user.enums.Role;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.apiPayload.code.status.ErrorStatus;
import com.server.money_touch.global.apiPayload.exception.handler.ErrorHandler;
import com.server.money_touch.global.config.jwt.TokenProvider;
import com.server.money_touch.domain.user.utils.KakaoUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoUtil kakaoUtil;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserQueryService userQueryService;
    private final BudgetCommandService budgetCommandService;

    public UserResponse.OAuthLoginResultDTO oAuthLogin(String accessCode, String redirectUrl,HttpServletResponse httpServletResponse) {
        // 1. access token 요청 시 동적 redirectUri 전달
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode, redirectUrl);

        // 2. 카카오 프로필 조회
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);
        String email = kakaoProfile.getKakaoAccount().getEmail();

        // 사용자 존재 여부 확인
        User user;
        boolean isNewUser = false;

        if (userQueryService.existsByEmail(email)) {
            user = userRepository.findByEmail(email).get();
        } else {
            user = createNewUser(kakaoProfile); // 등록되지않은 회원이면 회원가입
            isNewUser = true;
        }

        // ✅ 신규 가입자에 대해서만 예산 및 소비 생성
        if (isNewUser) {
            // Budget, 기본 ConsumptionCategory 테이블 조회 or 생성
            Budget budget = budgetCommandService.createOrFindBudgetForMonth(user);
            budgetCommandService.saveCategoryBudgetsByType(null, user, budget, CategoryType.DEFAULT);
        }

        // 1. User를 기반으로 CustomUserDetails 생성
        CustomUserDetails userDetails = new CustomUserDetails(user, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // 2. 토큰 생성
        TokenResponse tokenResponse = tokenProvider.generateTokens(authentication);
        // 3. 헤더에 토큰 담기
        httpServletResponse.setHeader("Authorization", "Bearer " + tokenResponse.getAccessToken());

        return UserResponse.OAuthLoginResultDTO.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .isNewUser(isNewUser)
                .build();
    }

    @Transactional
    public User createNewUser(KakaoDTO.KakaoProfile kakaoProfile) {
        String email = kakaoProfile.getKakaoAccount().getEmail();
        String kakaoKey = String.valueOf(kakaoProfile.getId());
        String nickname = kakaoProfile.getKakaoAccount().getProfile().getNickname();

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(nickname)) {
            throw new ErrorHandler(ErrorStatus.NICKNAME_ALREADY_EXISTS);
        }

        // 1. User 생성
        User newUser = User.builder()
                .nickname(nickname)
                .email(email)
                .authType(AuthType.KAKAO)
                .role(Role.USER)
                .build();

        // 2. SocialLogin 생성 & 양방향 연결
        SocialLogin socialLogin = SocialLogin.builder()
                .KakaoKey(kakaoKey)
                .user(newUser)
                .build();

        newUser.setSocialLogin(socialLogin);

        // 3. Cascade 설정이 있으면 이 한 줄로 둘 다 저장됨
        return userRepository.save(newUser);
    }
}
