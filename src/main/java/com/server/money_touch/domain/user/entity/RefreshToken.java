package com.server.money_touch.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "user_email" , nullable = false, unique = true)
    private String email;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "expriry", nullable = false)
    private LocalDateTime expiry;

    public RefreshToken(String email, String refreshToken, LocalDateTime expiry) {
        this.email = email;
        this.refreshToken = refreshToken;
        this.expiry = expiry;
    }

    public void update(String newToken, LocalDateTime newExpiry) {
        this.refreshToken = newToken;
        this.expiry = newExpiry;
    }


}
