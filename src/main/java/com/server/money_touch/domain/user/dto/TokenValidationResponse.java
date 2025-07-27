package com.server.money_touch.domain.user.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponse {
    private boolean valid;
    private Date expiration;
}