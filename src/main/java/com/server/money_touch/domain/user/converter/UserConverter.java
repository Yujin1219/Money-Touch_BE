package com.server.money_touch.domain.user.converter;

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
    public static User createLocalUser(String email, String encodedPassword, String age, Gender gender, String job, Boolean isIncome) {
        // User 엔티티 생성
        User user = User.builder()
                .email(email)
                .authType(AuthType.LOCAL)
                .role(Role.USER)
                .build();

        // UserDetail 생성
        UserDetail userDetail = createUserDetail(age, gender, job, isIncome);
        user.setUserDetail(userDetail);

        // LocalLogin 생성
        LocalLogin localLogin = createLocalLogin(encodedPassword, user);
        user.setLocalLogin(localLogin);

        return user;
    }

    /**
     * 소셜 회원가입용 User 생성
     */
    public static User createSocialUser(String email, String kakaoKey, String age, Gender gender, String job, Boolean isIncome) {
        // User 엔티티 생성
        User user = User.builder()
                .email(email)
                .authType(AuthType.KAKAO)
                .role(Role.USER)
                .build();

        // UserDetail 생성
        UserDetail userDetail = createUserDetail(age, gender, job, isIncome);
        user.setUserDetail(userDetail);

        // SocialLogin 생성
        SocialLogin socialLogin = createSocialLogin(kakaoKey, user);
        user.setSocialLogin(socialLogin);

        return user;
    }

    /**
     * UserDetail 생성
     */
    public  static UserDetail createUserDetail(String age, Gender gender, String job, Boolean isIncome) {
        return UserDetail.builder()
                .age(age)
                .gender(gender)
                .job(job)
                .isIncome(isIncome)
                .build();
    }

    /**
     * LocalLogin 생성
     */
    public static LocalLogin createLocalLogin(String encodedPassword, User user) {
        LocalLogin localLogin = LocalLogin.builder()
                .password(encodedPassword)
                .build();
        localLogin.setUser(user);
        return localLogin;
    }

    /**
     * SocialLogin 생성
     */
    public static SocialLogin createSocialLogin(String kakaoKey, User user) {
        SocialLogin socialLogin = SocialLogin.builder()
                .KakaoKey(kakaoKey)
                .connectedAt(LocalDateTime.now())
                .build();
        socialLogin.setUser(user);
        return socialLogin;
    }


}
