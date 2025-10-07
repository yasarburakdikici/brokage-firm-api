package com.brokage.challenge.service;

import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.entity.Order;

public interface OrderCancellationStrategy {
    OrderSide getSupportedSide();
    void refundUsableBalance(Order order);
}
