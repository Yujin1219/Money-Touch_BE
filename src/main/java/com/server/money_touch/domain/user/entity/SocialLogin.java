package com.server.money_touch.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class SocialLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String KakaoKey;

    @CreatedDate
    private LocalDateTime connectedAt;

    // 회원-소셜로그인 일대일
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
