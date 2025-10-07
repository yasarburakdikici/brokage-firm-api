package com.brokage.challenge.service.impl;

import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.exception.InvalidOrderException;
import com.brokage.challenge.service.OrderCancellationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AssetUpdateManager {
    private final Map<OrderSide, OrderCancellationStrategy> strategies;

    public AssetUpdateManager(List<OrderCancellationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(OrderCancellationStrategy::getSupportedSide, Function.identity()));
    }

    @Transactional
    public void refundUsableBalanceForCancellation(Order order) {
        OrderCancellationStrategy strategy = strategies.get(order.getOrderSide());

        if (strategy == null) {
            throw new InvalidOrderException(
                    "No cancellation strategy found for order side: " + order.getOrderSide()
            );
        }

        strategy.refundUsableBalance(order);
    }
}
