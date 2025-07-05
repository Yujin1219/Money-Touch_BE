package com.server.money_touch.consumptionRecord.entity;

import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ConsumptionRecord extends BaseEntity {

    @Column(nullable = false)
    private int amount;

    @Column(length = 20, nullable = false)
    private String content;

    private boolean isPublic = true;

    private String imageUrl;

    @Column(length = 1000)
    private String memo;


}
