package com.server.money_touch.domain.user.entity;


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

    private Long badgeId;

    // 권한 타입 : 기본값 USER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

}
