package com.server.money_touch.domain.user.entity;

import com.server.money_touch.domain.user.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class UserDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String job;

    @Column(nullable = false)
    private Boolean isIncome;

    // 회원-회원상세 일대일
    @OneToOne(mappedBy = "userDetail", fetch = FetchType.LAZY)
    private User user;

}
