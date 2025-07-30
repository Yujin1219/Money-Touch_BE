package com.server.money_touch.domain.user.service.user;

import com.server.money_touch.domain.user.converter.AuthConverter;
import com.server.money_touch.domain.user.dto.KakaoDTO;
import com.server.money_touch.domain.user.dto.TokenResponse;
import com.server.money_touch.domain.user.entity.CustomUserDetails;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import com.server.money_touch.global.config.jwt.TokenProvider;
import com.server.money_touch.domain.user.utils.KakaoUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoUtil kakaoUtil;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public User oAuthLogin(String accessCode, HttpServletResponse httpServletResponse) {
        KakaoDTO.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);
        KakaoDTO.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);
        String email = kakaoProfile.getKakao_account().getEmail();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(kakaoProfile)); // 등록되지않은 회원이면 회원가입
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
                kakaoProfile.getKakao_account().getEmail(),
                kakaoProfile.getKakao_account().getProfile().getNickname(),
                null,
                passwordEncoder
        );
        return userRepository.save(newUser);
    }
}
