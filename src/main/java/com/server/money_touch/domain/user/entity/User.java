package com.server.money_touch.domain.user.entity;


import com.server.money_touch.domain.user.enums.AuthType;
import com.server.money_touch.domain.user.enums.Role;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthType authType;

    @Column(nullable = false, unique = true)
    private String nickname;

    private String profileImgUrl;

    // 대표 배지 id
    private Long badgeId;

    // 권한 타입 : 기본값 USER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private LocalLogin localLogin;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private SocialLogin socialLogin;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true) // 해당 user 삭제시 userDetail 자동삭제
    @JoinColumn(name = "user_detail_id")
    private UserDetail userDetail;


}
