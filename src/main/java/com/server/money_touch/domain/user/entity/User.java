package com.server.money_touch.domain.user.entity;


import com.server.money_touch.domain.user.enums.AuthType;
import com.server.money_touch.domain.user.enums.Role;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 10)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthType authType;

    private String profileImgUrl;

    // 대표 배지 id
    private Long badgeId;

    // 권한 타입 : 기본값 USER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;


    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true) // 해당 user 삭제시 userDetail 자동삭제
    @JoinColumn(name = "user_detail_id")
    private UserDetail userDetail;
}
