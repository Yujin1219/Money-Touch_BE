package com.server.money_touch.domain.user.entity;


import com.server.money_touch.domain.badge.entity.Badge;
import com.server.money_touch.domain.badge.entity.UserBadge;
import com.server.money_touch.domain.budget.entity.Budget;
import com.server.money_touch.domain.budget.entity.BudgetCategory;
import com.server.money_touch.domain.consumptionMbti.entity.ConsumptionMbti;
import com.server.money_touch.domain.consumptionRecord.entity.*;
import com.server.money_touch.domain.fixedConsumption.entity.FixedConsumption;
import com.server.money_touch.domain.notification.entity.Notification;
import com.server.money_touch.domain.routine.entity.Routine;
import com.server.money_touch.domain.user.enums.AuthType;
import com.server.money_touch.domain.user.enums.Role;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false, unique = true)
    private String nickname;

    private String profileImgUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthType authType;

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

    // 회원 삭제를 위한 Cascade 설정

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsumptionCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TotalConsumption> totalConsumption;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsumptionRecord> consumptionRecords = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBadge> badges = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedConsumption> fixedConsumptionList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Routine> routines  = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsumptionMbti> consumptionMbtiList  = new ArrayList<>();

}
