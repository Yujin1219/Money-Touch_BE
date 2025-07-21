package com.server.money_touch.domain.consumptionRecord.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

// 가계부 달력 월별 소비 금액 조회 - 특정 날짜의 소비 내역 조회용 QueryDSL 프로젝션
@Getter
public class DailyAmountProjection {
    private LocalDate date;
    private Integer totalAmount;

    public DailyAmountProjection(String dateString, Integer totalAmount) {
        this.date = LocalDate.parse(dateString);  // "2025-07-02" → LocalDate
        this.totalAmount = totalAmount;
    }
}