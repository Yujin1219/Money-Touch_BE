package com.server.money_touch.domain.term.entity;

import com.server.money_touch.global.apiPayload.code.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Terms extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean isRequired = true;
}
