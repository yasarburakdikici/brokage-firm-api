package com.brokage.challenge.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.enums.OrderStatus;

public record OrderResponse(
            String customerId,
            String asset,
            OrderSide side,
            Long size,
            BigDecimal price,
            OrderStatus status,
            Instant createDate
        ) {}



