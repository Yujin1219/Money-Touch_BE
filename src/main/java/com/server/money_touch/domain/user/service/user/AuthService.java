package com.server.money_touch.domain.user.service.user;

import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.enums.CategoryType;
import com.server.money_touch.domain.budget.service.budget.BudgetCommandService;
import com.server.money_touch.domain.consumptionRecord.converter.totalConsumption.TotalConsumptionConverter;
import com.server.money_touch.domain.consumptionRecord.entity.TotalConsumption;
import com.server.money_touch.domain.consumptionRecord.repository.totalConsumption.TotalConsumptionRepository;
import com.server.money_touch.domain.user.converter.AuthConverter;
import com.server.money_touch.domain.user.dto.KakaoDTO;
import com.server.money_touch.domain.user.dto.TokenResponse;
import com.server.money_touch.domain.user.entity.CustomUserDetails;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.enums.AuthType;
import com.server.money_touch.domain.user.enums.Role;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.config.jwt.TokenProvider;
import com.server.money_touch.domain.user.utils.KakaoUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final TotalConsumptionRepository totalConsumptionRepository;

    public User oAuthLogin(String accessCode, HttpServletResponse httpServletResponse) {
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);
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
            LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

            Budget budget = budgetCommandService.createOrFindBudgetForMonth(user);
            budgetCommandService.saveCategoryBudgetsByType(null, user, budget, CategoryType.DEFAULT);

            totalConsumptionRepository.findByUserAndCreatedAtBetween(user, startOfMonth, endOfMonth)
                    .orElseGet(() -> totalConsumptionRepository.save(
                            TotalConsumptionConverter.toTotalConsumption(user))
                    );
        }

        // 1. User를 기반으로 CustomUserDetails 생성
        CustomUserDetails userDetails = new CustomUserDetails(user, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // 2. 토큰 생성
        TokenResponse tokenResponse = tokenProvider.generateTokens(authentication);
        // 3. 헤더에 토큰 담기
        httpServletResponse.setHeader("Authorization", "Bearer " + tokenResponse.getAccessToken());

        return user;
    }

    private User createNewUser(KakaoDTO.KakaoProfile kakaoProfile) {
        User newUser = AuthConverter.toUser(
                kakaoProfile.getKakaoAccount().getEmail(),
                kakaoProfile.getKakaoAccount().getProfile().getNickname(),
                null,
                passwordEncoder,
                Role.USER,
                AuthType.KAKAO
        );

        return userRepository.save(newUser);
    }
}
