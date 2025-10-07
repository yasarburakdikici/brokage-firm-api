package com.brokage.challenge.mapper;

import com.brokage.challenge.dto.OrderResponse;
import com.brokage.challenge.entity.Order;

public class OrderResponseMapper {

    private OrderResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
                order.getCustomerId(),
                order.getAssetName(),
                order.getOrderSide(),
                order.getSize(),
                order.getPrice(),
                order.getStatus(),
                order.getCreateDate()
        );
    }
}
