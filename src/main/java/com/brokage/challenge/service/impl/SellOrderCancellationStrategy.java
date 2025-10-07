package com.brokage.challenge.service.impl;

import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.service.AssetService;
import com.brokage.challenge.service.OrderCancellationStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SellOrderCancellationStrategy implements OrderCancellationStrategy {
    private final AssetService assetService;

    public SellOrderCancellationStrategy(AssetService assetService) {
        this.assetService = assetService;
    }

    @Override
    public OrderSide getSupportedSide() {
        return OrderSide.SELL;
    }

    @Override
    public void refundUsableBalance(Order order) {
        BigDecimal reservedShares = BigDecimal.valueOf(order.getSize());
        assetService.increaseUsableSize(order.getCustomerId(), order.getAssetName(), reservedShares);
    }
}
