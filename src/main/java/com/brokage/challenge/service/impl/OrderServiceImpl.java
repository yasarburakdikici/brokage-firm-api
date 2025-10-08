package com.brokage.challenge.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.brokage.challenge.audit.Auditable;
import com.brokage.challenge.exception.BrokageFirmApiException;
import com.brokage.challenge.exception.InvalidOrderException;
import com.brokage.challenge.exception.InvalidCustomerException;
import com.brokage.challenge.exception.InvalidAssetException;
import com.brokage.challenge.service.CreateOrderProcessor;
import com.brokage.challenge.service.OrderService;
import com.brokage.challenge.util.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brokage.challenge.dto.CreateOrder;
import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.enums.OrderStatus;
import com.brokage.challenge.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    
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
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("CREATE_ORDER", log);
        
        try {
            log.info("Creating order for customer: {}, asset: {}, side: {}, size: {}, price: {}", 
                    request.customer(), request.asset(), request.side(), request.size(), request.price());
            
            CreateOrderProcessor processor = processorMap.get(request.side());
            if (processor == null) {
                String errorMsg = String.format("Unsupported order side: %s", request.side());
                log.error("Order creation failed: {}", errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            
            Order order = processor.process(request);
            log.info("Order created successfully with ID: {} for customer: {}", 
                    order.getId(), request.customer());
            
            return order;
            
        } catch (InvalidOrderException | InvalidCustomerException | InvalidAssetException | IllegalArgumentException e) {
            timer.finishWithError(e.getMessage());
            log.error("Order creation failed for customer: {} - Business Error: {}", 
                     request.customer(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("Order creation failed for customer: {} - System Error: {}", 
                     request.customer(), e.getMessage(), e);
            throw new BrokageFirmApiException("Order creation failed due to system error", e);
        } finally {
            timer.finish();
        }
    }


    @Override
    public List<Order> listOrders(String customerId, Instant startDate, Instant endDate) {
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("LIST_ORDERS", log);
        
        try {
            log.info("Listing orders for customer: {} between {} and {}", 
                    customerId, startDate, endDate);
            
            List<Order> orders = orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
            
            log.info("Found {} orders for customer: {}", orders.size(), customerId);
            return orders;
            
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("Failed to list orders for customer: {} - System Error: {}", 
                     customerId, e.getMessage(), e);
            throw new BrokageFirmApiException("Failed to list orders due to system error", e);
        } finally {
            timer.finish();
        }
    }

    @Transactional
    @Override
    @Auditable(operation = "DELETE_ORDER", entityType = "Order")
    public void deleteOrder(Long orderId) {
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("DELETE_ORDER", log);
        
        try {
            log.info("Deleting order with ID: {}", orderId);
            
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new InvalidOrderException(String.format("Order not found for this order id: %d", orderId)));

            if (!OrderStatus.PENDING.equals(order.getStatus())) {
                String errorMsg = String.format("Order not found for this order id: %d", orderId);
                log.error("Order deletion failed: {}", errorMsg);
                throw new InvalidOrderException(errorMsg);
            }

            log.info("Refunding balance for cancelled order - ID: {}, customer: {}, side: {}", 
                    order.getId(), order.getCustomerId(), order.getOrderSide());
            
            assetUpdateManager.refundUsableBalanceForCancellation(order);
            orderRepository.delete(order);
            
            log.info("Order deleted successfully: {}", orderId);
            
        } catch (InvalidOrderException e) {
            timer.finishWithError(e.getMessage());
            log.error("Order deletion failed for ID: {} - Business Error: {}", 
                     orderId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("Order deletion failed for ID: {} - System Error: {}", 
                     orderId, e.getMessage(), e);
            throw new BrokageFirmApiException("Order deletion failed due to system error", e);
        } finally {
            timer.finish();
        }
    }
}


