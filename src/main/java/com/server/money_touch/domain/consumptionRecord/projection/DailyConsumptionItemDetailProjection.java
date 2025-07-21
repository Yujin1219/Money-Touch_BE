package com.server.money_touch.domain.consumptionRecord.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 달력 - 특정 날짜의 소비 내역 조회용 QueryDSL 프로젝션
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyConsumptionItemDetailProjection {
    private Long consumptionRecordId;
    private String categoryName;
    private String content;
    private Integer amount;

}
