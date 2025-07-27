package com.server.money_touch.domain.user.service.user;

import com.server.money_touch.domain.user.converter.UserConverter;
import com.server.money_touch.domain.user.dto.TokenResponse;
import com.server.money_touch.domain.user.dto.UserRequest;
import com.server.money_touch.domain.user.dto.UserResponse;
import com.server.money_touch.domain.user.entity.LocalLogin;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.enums.AuthType;
import com.server.money_touch.domain.user.enums.Role;
import com.server.money_touch.domain.user.repository.user.UserRepository;

//import com.server.money_touch.global.external.kakao.KakaoService;
//import com.server.money_touch.global.external.kakao.dto.KakaoUserInfo;
import com.server.money_touch.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    /**
     * 로컬 회원가입
     */
    @Override
    @Transactional
    public UserResponse.UserCreateResultDTO signUpLocal(UserRequest.LocalSignUpDTO request) {
        log.info("로컬 회원가입 시작 - email: {}", request.getEmail());

        // 이메일 중복 검증
        validateDuplicateEmail(request.getEmail());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 생성 (기본 정보만)
        User user = User.builder()
                .email(request.getEmail())
                .authType(AuthType.LOCAL)
                .role(Role.USER)
                .nickname(request.getNickname())
                .build();

        // LocalLogin 생성
        LocalLogin localLogin = UserConverter.createLocalLogin(encodedPassword, user);
        user.setLocalLogin(localLogin);

        // User 저장
        User savedUser = userRepository.save(user);

        // 약관 동의 처리
        processAgreements(savedUser, request.getAgreeTerms());

        log.info("로컬 회원가입 완료 - userId: {}", savedUser.getId());

        return UserResponse.UserCreateResultDTO.builder()
                .userId(savedUser.getId())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    /**
     * 로컬 로그인
     */
    @Override
    @Transactional
    public UserResponse.LoginResultDTO loginLocal(UserRequest.LocalLoginDTO request) {
        log.info("로컬 로그인 시작 - email: {}", request.getEmail());

        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 로컬 로그인 사용자인지 확인
        if (user.getAuthType() != AuthType.LOCAL || user.getLocalLogin() == null) {
            throw new IllegalArgumentException("로컬 로그인 사용자가 아닙니다.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getLocalLogin().getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // Authentication 객체 생성
        Authentication authentication = createAuthentication(user);

        // JWT 토큰 생성
        TokenResponse tokenResponse = tokenProvider.generateTokens(authentication);

        log.info("로컬 로그인 완료 - userId: {}", user.getId());

        return UserResponse.LoginResultDTO.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .build();
    }

    /**
     * 토큰 갱신
     */
    @Override
    @Transactional
    public UserResponse.LoginResultDTO refreshToken(String refreshToken) {
        log.info("토큰 갱신 시작");

        // TokenProvider를 통해 토큰 갱신
        TokenResponse tokenResponse = tokenProvider.refreshAccessToken(refreshToken);

        if (tokenResponse == null) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        log.info("토큰 갱신 완료");

        return UserResponse.LoginResultDTO.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .build();
    }

    /**
     * Authentication 객체 생성
     */
    private Authentication createAuthentication(User user) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

        return new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singletonList(authority)
        );
    }

    /**
     * 이메일 중복 검증
     */
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
    }

    /**
     * 약관 동의 처리
     */
    private void processAgreements(User user, List<UserRequest.AgreementDTO> agreements) {
        if (agreements != null && !agreements.isEmpty()) {
            log.info("약관 동의 처리 - userId: {}, 동의 항목 수: {}", user.getId(), agreements.size());

            // 필수 약관 동의 검증
            boolean hasRequiredAgreements = agreements.stream()
                    .anyMatch(agreement -> agreement.getIsAgree() && isRequiredTerm(agreement.getTermsId()));

            if (!hasRequiredAgreements) {
                throw new IllegalArgumentException("필수 약관에 동의해야 합니다.");
            }

            // 약관 동의 정보 저장 로직 (추후 구현)
            // userAgreementService.saveAgreements(user.getId(), agreements);
        }
    }

    /**
     * 필수 약관 여부 확인
     */
    private boolean isRequiredTerm(Long termsId) {
        // 실제로는 Terms 엔티티에서 필수 여부를 확인해야 함
        // 예시로 ID 1, 2를 필수 약관으로 가정
        return termsId.equals(1L) || termsId.equals(2L);
    }
}