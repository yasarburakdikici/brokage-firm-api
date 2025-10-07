package com.brokage.challenge.service;

import com.brokage.challenge.dto.CreateOrder;
import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import jakarta.validation.Valid;

public interface CreateOrderProcessor {
    OrderSide getSupportedSide();
    Order process(@Valid CreateOrder request);
}
