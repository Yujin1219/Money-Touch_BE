package com.server.money_touch.domain.consumptionMbti.entity;

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
public class ConsumptionMbti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mbtiImgUrl;

    private String description;

    @Column(length = 10)
    private String result; // 소비MBTI 이름 ex) PTG

    @CreatedDate
    private LocalDateTime createdAt;

    // 소비MBTI-회원 다대일
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
