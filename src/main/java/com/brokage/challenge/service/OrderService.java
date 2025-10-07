package com.brokage.challenge.service;

import com.brokage.challenge.dto.CreateOrder;
import com.brokage.challenge.entity.Order;

import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;

public interface OrderService {
    Order createOrder(@Valid CreateOrder req);
    List<Order> listOrders(String customer, Instant startDate, Instant endDate);
    void deleteOrder(Long orderId);
}
