package com.server.money_touch.domain.user.entity;

import com.server.money_touch.domain.user.enums.AuthType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails, Serializable {

    private final Long id;
    private final String email;
    private final String password;
    private final String role;
    private final AuthType authType;

    public CustomUserDetails(User user, String password) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.authType = user.getAuthType();
        this.password = password; // local login 비밀번호
    }

    public Long getId() {
        return id;
    }

    public AuthType getAuthType() {
        return authType;
    }

    // 사용자의 이메일을 반환
    @Override
    public String getUsername() {
        return email;
    }

    // 사용자의 password 반환
    @Override
    public String getPassword() {
        return password; // localLogin.getPassword()로 받은 값
    }

    // 권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public boolean isAccountNonExpired() { return true; } // 계정 만료 여부 반환
    @Override public boolean isAccountNonLocked() { return true; } // 계쩡 잠금 여부 반환
    @Override public boolean isCredentialsNonExpired() { return true; } // 패스워드 만료 여부 반환
    @Override public boolean isEnabled() { return true; } // 계쩡 사용 가능 여부 반환
}
