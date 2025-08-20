package com.server.money_touch.domain.consumptionRecord.entity;

import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ConsumptionRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "consumption_category_id", nullable = false)
    private ConsumptionCategory consumptionCategory;

    @Column(nullable = false)
    private int amount;

    @Column(length = 20, nullable = false)
    private String content;

    private boolean isPublic = true;

    @OneToMany(mappedBy = "consumptionRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsumptionRecordImage> images = new ArrayList<>();

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

    private LocalDateTime consumeDate; // 소비 기록 날짜

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    @Builder.Default
    private Boolean isFixed = false; // 고정비 여부

    @OneToMany(mappedBy = "consumptionRecord", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "consumptionRecord", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 일일 소비 기록 수정
    public void updateDailyConsumptionRecord(ConsumptionCategory category, int amount, String content, String memo, LocalDateTime consumeDate) {
        this.consumptionCategory = category;
        this.amount = amount;
        this.content = content;
        this.memo = memo;
        this.consumeDate = consumeDate;
    }
}