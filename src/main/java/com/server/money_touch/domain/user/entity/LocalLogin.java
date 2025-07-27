package com.server.money_touch.domain.user.entity;

import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class LocalLogin extends BaseEntity {

    // 회원-로컬로그인 일대일
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 15)
    private String password;


    public void encodePassword(String password) {
        this.password = password;
    }
}
