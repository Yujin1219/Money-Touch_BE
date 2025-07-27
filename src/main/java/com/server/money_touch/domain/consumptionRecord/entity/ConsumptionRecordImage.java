package com.server.money_touch.domain.consumptionRecord.entity;

import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ConsumptionRecordImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumption_record_id",nullable = false)
    private ConsumptionRecord consumptionRecord;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String filePath;

}
