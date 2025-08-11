package com.server.money_touch.domain.user.converter;

import com.server.money_touch.domain.badge.entity.Badge;
import com.server.money_touch.domain.user.dto.UserRequest;
import com.server.money_touch.domain.user.dto.UserResponse;
import com.server.money_touch.domain.user.entity.LocalLogin;
import com.server.money_touch.domain.user.entity.SocialLogin;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.entity.UserDetail;
import com.server.money_touch.domain.user.enums.AuthType;
import com.server.money_touch.domain.user.enums.Gender;
import com.server.money_touch.domain.user.enums.Role;

import java.time.LocalDateTime;


public class UserConverter {

    // 로컬 회원가입용 User 생성
    public static User createLocalUser(String email, String encodedPassword, UserRequest.UserDetailCreateDTO requestDTO) {
        // User 엔티티 생성
        User user = User.builder()
                .email(email)
                .authType(AuthType.LOCAL)
                .role(Role.USER)
                .build();

        // UserDetail 및 LocalLogin 생성 (user에 연관관계 주입)
        UserDetail userDetail = createUserDetail(user, requestDTO);
        LocalLogin localLogin = createLocalLogin(encodedPassword, user);

        // 연관 엔티티 설정
        user.setUserDetail(userDetail);
        user.setLocalLogin(localLogin);

        return user;
    }

    // 소셜 회원가입용 User 생성
    public static User createSocialUser(String email, String kakaoKey) {
        // User 엔티티 생성
        User user = User.builder()
                .email(email)
                .authType(AuthType.KAKAO)
                .role(Role.USER)
                .build();

        //SocialLogin 생성
        SocialLogin socialLogin = createSocialLogin(kakaoKey, user);
        user.setSocialLogin(socialLogin);

        return user;
    }

    // ✅ User → DTO 변환
    public static UserResponse.UserCreateResultDTO toUserCreateResultDTO(User user) {
        return UserResponse.UserCreateResultDTO.builder()
                .userId(user.getId())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ✅ 유저 상세정보 엔티티 생성 (User 참조 포함)
    public static UserDetail createUserDetail(User user, UserRequest.UserDetailCreateDTO requestDTO) {
        return UserDetail.builder()
                .user(user)
                .gender(requestDTO.getGender())
                .job(requestDTO.getJob())
                .age(requestDTO.getAge())
                .isIncome(requestDTO.getIsIncome())
                .build();
    }

    // 유저 상세정보 등록 응답 생성
    public static UserResponse.UserDetailCreateResultDTO toUserDetailCreateResultDTO(Long userDetailId) {
        return UserResponse.UserDetailCreateResultDTO.builder()
                .userDetailId(userDetailId)
                .build();
    }

    // ✅ 로컬 로그인 엔티티 생성
    public static LocalLogin createLocalLogin(String encodedPassword, User user) {
        return LocalLogin.builder()
                .user(user)
                .password(encodedPassword)
                .build();
    }

    // ✅ 소셜 로그인 엔티티 생성
    public static SocialLogin createSocialLogin(String kakaoKey, User user) {
        return SocialLogin.builder()
                .user(user)
                .KakaoKey(kakaoKey)
                .build();
    }

    // 마이페이지용
    public static UserResponse.MyPageResponseDTO toMyPageResponseDTO(User user, Badge badge){

        return UserResponse.MyPageResponseDTO.builder()
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImgUrl())
                .representativeBadgeImageUrl(badge != null ? badge.getImageUrl() : null)
                .build();
    }

}
