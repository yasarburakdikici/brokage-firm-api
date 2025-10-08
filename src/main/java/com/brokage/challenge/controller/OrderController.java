package com.brokage.challenge.controller;

import com.brokage.challenge.mapper.OrderResponseMapper;
import com.brokage.challenge.entity.Order;
import com.brokage.challenge.exception.BrokageFirmApiException;
import com.brokage.challenge.exception.InvalidOrderException;
import com.brokage.challenge.exception.InvalidCustomerException;
import com.brokage.challenge.exception.InvalidAssetException;
import com.brokage.challenge.service.impl.OrderServiceImpl;
import com.brokage.challenge.util.TimeUtil;

import jakarta.validation.Valid;

import com.brokage.challenge.dto.OrderResponse;
import com.brokage.challenge.dto.CreateOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    
    private final OrderServiceImpl orderService;

    public OrderController(OrderServiceImpl orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid CreateOrder req) {
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("CREATE_ORDER_CONTROLLER", log);
        
        try {
            log.info("Order creation request received for customer: {}, asset: {}, side: {}", 
                    req.customer(), req.asset(), req.side());
            
            Order order = orderService.createOrder(req);
            OrderResponse response = OrderResponseMapper.toOrderResponse(order);
            
            log.info("Order creation response prepared for customer: {}, order ID: {}", 
                    req.customer(), order.getId());
            
            return response;
            
        } catch (InvalidOrderException | InvalidCustomerException | InvalidAssetException e) {
            timer.finishWithError(e.getMessage());
            log.error("Order creation request failed for customer: {} - Business Error: {}", 
                     req.customer(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("Order creation request failed for customer: {} - System Error: {}", 
                     req.customer(), e.getMessage(), e);
            throw new BrokageFirmApiException("Order creation request failed due to system error", e);
        } finally {
            timer.finish();
        }
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponse> listOrders(
            @RequestParam String customer,
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("LIST_ORDERS_CONTROLLER", log);
        
        try {
            log.info("Order list request received for customer: {} between {} and {}", 
                    customer, startDate, endDate);
            
            List<OrderResponse> responses = orderService.listOrders(customer, startDate, endDate)
                    .stream()
                    .map(OrderResponseMapper::toOrderResponse)
                    .toList();
            
            log.info("Order list response prepared for customer: {}, found {} orders", 
                    customer, responses.size());
            
            return responses;
            
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("Order list request failed for customer: {} - System Error: {}", 
                     customer, e.getMessage(), e);
            throw new BrokageFirmApiException("Order list request failed due to system error", e);
        } finally {
            timer.finish();
        }
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long orderId) {
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("DELETE_ORDER_CONTROLLER", log);
        
        try {
            log.info("Order deletion request received for order ID: {}", orderId);
            
            orderService.deleteOrder(orderId);
            
            log.info("Order deletion completed for order ID: {}", orderId);
            
        } catch (InvalidOrderException e) {
            timer.finishWithError(e.getMessage());
            log.error("Order deletion request failed for order ID: {} - Business Error: {}", 
                     orderId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("Order deletion request failed for order ID: {} - System Error: {}", 
                     orderId, e.getMessage(), e);
            throw new BrokageFirmApiException("Order deletion request failed due to system error", e);
        } finally {
            timer.finish();
        }
    }
}


