package com.brokage.challenge.controller;

import com.brokage.challenge.mapper.OrderResponseMapper;
import com.brokage.challenge.entity.Order;
import com.brokage.challenge.service.impl.OrderServiceImpl;

import jakarta.validation.Valid;

import com.brokage.challenge.dto.OrderResponse;
import com.brokage.challenge.dto.CreateOrder;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderServiceImpl orderService;

    public OrderController(OrderServiceImpl orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid CreateOrder req) {
        Order order = orderService.createOrder(req);
        return OrderResponseMapper.toOrderResponse(order);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> listOrders(
            @RequestParam String customer,
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        return orderService.listOrders(customer, startDate, endDate)
                .stream()
                .map(OrderResponseMapper::toOrderResponse)
                .toList();
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
    }
}


