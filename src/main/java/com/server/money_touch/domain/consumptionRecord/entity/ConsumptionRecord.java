package com.server.money_touch.domain.consumptionRecord.entity;

import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ConsumptionRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumption_category_id", nullable = false)
    private ConsumptionCategory consumptionCategory;

    @Column(nullable = false)
    private int amount;

    @Column(length = 20, nullable = false)
    private String content;

    private boolean isPublic = true;

    private String imageUrl;

    @Column(length = 1000)
    private String memo;

    @ColumnDefault("0")
    private Integer commentCount = 0;

    @ColumnDefault("0")
    private Integer wiseCount = 0;

    @ColumnDefault("0")
    private Integer wasteCount = 0;

    @ColumnDefault("0")
    private Integer viewCount = 0;

    private LocalDateTime consumeDate; // 일일 소비 기록 날짜

    // 일일 소비 기록 수정
    public void updateDailyConsumptionRecord(ConsumptionCategory category, int amount, String content, String memo, LocalDateTime consumeDate) {
        this.consumptionCategory = category;
        this.amount = amount;
        this.content = content;
        this.memo = memo;
        this.consumeDate = consumeDate;
    }
}