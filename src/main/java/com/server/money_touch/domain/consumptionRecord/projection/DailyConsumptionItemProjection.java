package com.server.money_touch.domain.consumptionRecord.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyConsumptionItemProjection {
    private Long consumptionRecordId;
    private String categoryName;
    private String content;
    private Integer amount;
    private LocalDateTime consumeDate;
}
