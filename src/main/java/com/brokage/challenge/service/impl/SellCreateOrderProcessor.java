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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SellCreateOrderProcessor implements CreateOrderProcessor {

    private final AssetRepository assetRepository;
    private final OrderRepository orderRepository;

    public SellCreateOrderProcessor(AssetRepository assetRepository, OrderRepository orderRepository) {
        this.assetRepository = assetRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderSide getSupportedSide() {
        return OrderSide.SELL;
    }

    @Transactional
    @Override
    public Order process(CreateOrder request) {
        Asset assetToSell = assetRepository.findByCustomerIdAndAssetName(request.customer(), request.asset())
                .orElseThrow(() -> new InvalidCustomerException(
                        request.customer() + " does not have a " + request.asset() + " asset."
                ));

        if (assetToSell.getUsableSize() < request.size()) {
            throw new InvalidCustomerException(
                    String.format("Insufficient %s balance for customer %s.",
                            request.asset(), request.customer())
            );
        }

        assetToSell.setUsableSize(assetToSell.getUsableSize() - request.size());
        assetRepository.save(assetToSell);

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

