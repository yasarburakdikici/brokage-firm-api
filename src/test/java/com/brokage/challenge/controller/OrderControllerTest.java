package com.brokage.challenge.controller;

import com.brokage.challenge.dto.CreateOrder;
import com.brokage.challenge.dto.OrderResponse;
import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.enums.OrderStatus;
import com.brokage.challenge.mapper.OrderResponseMapper;
import com.brokage.challenge.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderServiceImpl orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    @DisplayName("create returns mapped response and 201 status")
    void create_success() {
        // Arrange
        CreateOrder request = new CreateOrder("cust1", OrderSide.BUY, "BTC", 2L, new BigDecimal("10.50"));

        Order order = Order.builder()
                .customerId("cust1")
                .assetName("BTC")
                .orderSide(OrderSide.BUY)
                .size(2L)
                .price(new BigDecimal("10.50"))
                .status(OrderStatus.PENDING)
                .createDate(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        when(orderService.createOrder(request)).thenReturn(order);
        // Act
        OrderResponse response = orderController.create(request);
        // Assert
        assertThat(response).isEqualTo(OrderResponseMapper.toOrderResponse(order));
        verify(orderService, times(1)).createOrder(request);
    }

    @Test
    @DisplayName("delete returns 204 and delegates to service")
    void delete_success() {
        // Arrange
        Long orderId = 42L;
        // Act
        orderController.deleteOrder(orderId);
        // Assert
        verify(orderService, times(1)).deleteOrder(orderId);
    }

    @Test
    @DisplayName("list success for given customer and date range")
    void list_success() {
        // Arrange
        Order order = Order.builder()
                .customerId("cust1")
                .assetName("BTC")
                .orderSide(OrderSide.BUY)
                .size(2L)
                .price(new BigDecimal("10.50"))
                .status(OrderStatus.PENDING)
                .createDate(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        when(orderService.listOrders("cust1", Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-31T23:59:59Z")))
                .thenReturn(java.util.List.of(order));
        // Act
        List<OrderResponse> orderList = orderController.listOrders("cust1", Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-31T23:59:59Z"));
        // Assert
        assertThat(orderList).hasSize(1);
        assertEquals(orderList, java.util.List.of(OrderResponseMapper.toOrderResponse(order)));
        verify(orderService, times(1)).listOrders("cust1", Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-31T23:59:59Z"));
    }
}


