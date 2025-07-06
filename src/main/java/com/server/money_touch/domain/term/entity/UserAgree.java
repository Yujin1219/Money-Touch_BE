package com.server.money_touch.domain.term.entity;

import com.server.money_touch.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserAgree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isAgree;

    @CreatedDate
    private LocalDateTime agreeDate;

    //약관동의 - 회원 일대일
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    //약관동의 - 약관 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private Terms terms;
}
