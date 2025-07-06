package com.server.money_touch.domain.user.entity;

import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LocalLogin extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 15)
    private String password;

    // 회원-로컬로그인 일대일
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
