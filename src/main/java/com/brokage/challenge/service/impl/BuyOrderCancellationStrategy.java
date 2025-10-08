package com.brokage.challenge.service.impl;

import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.service.AssetService;
import com.brokage.challenge.service.OrderCancellationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BuyOrderCancellationStrategy implements OrderCancellationStrategy {
    private final AssetService assetService;

    public BuyOrderCancellationStrategy(AssetService assetService) {
        this.assetService = assetService;
    }

    @Override
    public OrderSide getSupportedSide() {
        return OrderSide.BUY;
    }

    @Override
    @Transactional
    public void refundUsableBalance(Order order) {
        Long reservedAmount = order.getPrice().multiply(BigDecimal.valueOf(order.getSize())).longValue();
        assetService.increaseUsableSize(order.getCustomerId(), "TRY", reservedAmount);
    }
}
