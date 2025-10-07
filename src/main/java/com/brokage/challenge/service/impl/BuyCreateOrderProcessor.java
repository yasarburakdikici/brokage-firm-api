package com.brokage.challenge.service.impl;

import com.brokage.challenge.exception.InvalidCustomerException;
import com.brokage.challenge.dto.CreateOrder;
import com.brokage.challenge.entity.Asset;
import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.enums.OrderStatus;
import com.brokage.challenge.repository.AssetRepository;
import com.brokage.challenge.repository.OrderRepository;
import com.brokage.challenge.service.CreateOrderProcessor;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class BuyCreateOrderProcessor implements CreateOrderProcessor {

    private final AssetRepository assetRepository;
    private final OrderRepository orderRepository;

    public BuyCreateOrderProcessor(AssetRepository assetRepository, OrderRepository orderRepository) {
        this.assetRepository = assetRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderSide getSupportedSide() {
        return OrderSide.BUY;
    }

    @Transactional
    @Override
    public Order process(@Valid CreateOrder request) {
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(request.customer(), "TRY")
                .orElseThrow(() -> new InvalidCustomerException(
                        request.customer() + " does not have a TRY asset."
                ));

        BigDecimal totalCost = request.price().multiply(BigDecimal.valueOf(request.size()));

        if (tryAsset.getUsableSize().compareTo(totalCost) < 0) {
            throw new InvalidCustomerException(
                    String.format("Insufficient TRY balance for customer %s.", request.customer())
            );
        }

        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalCost));
        assetRepository.save(tryAsset);

        return saveOrder(request);
    }

    private Order saveOrder(CreateOrder request) {
        Order newOrder = Order.builder()
                .customerId(request.customer())
                .assetName(request.asset())
                .orderSide(request.side())
                .price(request.price())
                .size(request.size())
                .status(OrderStatus.PENDING)
                .createDate(Instant.now())
                .build();

        return orderRepository.save(newOrder);
    }
}

