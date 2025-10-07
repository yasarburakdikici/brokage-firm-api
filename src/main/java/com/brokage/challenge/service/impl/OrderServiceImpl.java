package com.brokage.challenge.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.brokage.challenge.audit.Auditable;
import com.brokage.challenge.exception.InvalidOrderException;
import com.brokage.challenge.service.CreateOrderProcessor;
import com.brokage.challenge.service.OrderService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brokage.challenge.dto.CreateOrder;
import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.enums.OrderStatus;
import com.brokage.challenge.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final Map<OrderSide, CreateOrderProcessor> processorMap;
    private final AssetUpdateManager assetUpdateManager;

    public OrderServiceImpl(List<CreateOrderProcessor> processors, OrderRepository orderRepository, AssetUpdateManager assetUpdateManager) {
        this.processorMap = processors.stream()
                .collect(Collectors.toMap(CreateOrderProcessor::getSupportedSide, Function.identity()));
        this.orderRepository = orderRepository;
        this.assetUpdateManager = assetUpdateManager;
    }

    @Transactional
    @Override
    @Auditable(operation = "CREATE_ORDER", entityType = "Order")
    public Order createOrder(CreateOrder request) {
        CreateOrderProcessor processor = processorMap.get(request.side());
        if (processor == null) {
            throw new IllegalArgumentException(String.format("Unsupported order side: %s", request.side()));
        }
        return processor.process(request);
    }


    @Override
    public List<Order> listOrders(String customerId, Instant startDate, Instant endDate) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }

    @Transactional
    @Override
    @Auditable(operation = "DELETE_ORDER", entityType = "Order")
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException(String.format("Order not found for this order id: %d", orderId)));

        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            throw new InvalidOrderException(String.format("Order not found for this order id: %d", orderId));
        }

        assetUpdateManager.refundUsableBalanceForCancellation(order);
        orderRepository.delete(order);
    }
}


