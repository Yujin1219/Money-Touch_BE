package com.server.money_touch.domain.user.service.user;

import com.server.money_touch.domain.user.entity.CustomUserDetails;
import com.server.money_touch.domain.user.entity.LocalLogin;
import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.enums.AuthType;
import com.server.money_touch.domain.user.repository.user.LocalLoginRepository;
import com.server.money_touch.domain.user.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LocalLoginRepository localLoginRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일로 User 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        if (user.getAuthType() != AuthType.LOCAL) {
            throw new UsernameNotFoundException("소셜 로그인 유저입니다.");
        }

        // LocalLogin에서 password 가져오기
        LocalLogin localLogin = localLoginRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("비밀번호 정보가 없습니다."));

        return new CustomUserDetails(user, localLogin.getPassword());
    }

}
