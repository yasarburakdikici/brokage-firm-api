package com.brokage.challenge.service.impl;

import com.brokage.challenge.exception.BrokageFirmApiException;
import com.brokage.challenge.exception.InvalidCustomerException;
import com.brokage.challenge.dto.CreateOrder;
import com.brokage.challenge.entity.Asset;
import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.enums.OrderStatus;
import com.brokage.challenge.repository.AssetRepository;
import com.brokage.challenge.repository.OrderRepository;
import com.brokage.challenge.service.CreateOrderProcessor;
import com.brokage.challenge.util.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SellCreateOrderProcessor implements CreateOrderProcessor {

    private static final Logger log = LoggerFactory.getLogger(SellCreateOrderProcessor.class);
    
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
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("SELL_ORDER_PROCESS", log);
        
        try {
            log.info("Processing SELL order for customer: {}, asset: {}, size: {}, price: {}", 
                    request.customer(), request.asset(), request.size(), request.price());
            
            Asset assetToSell = assetRepository.findByCustomerIdAndAssetName(request.customer(), request.asset())
                    .orElseThrow(() -> new InvalidCustomerException(
                            request.customer() + " does not have a " + request.asset() + " asset."
                    ));

            if (assetToSell.getUsableSize() < request.size()) {
                String errorMsg = String.format("Insufficient %s balance for customer %s.",
                        request.asset(), request.customer());
                log.warn("Insufficient asset balance: {}", errorMsg);
                throw new InvalidCustomerException(errorMsg);
            }

            Long oldUsableSize = assetToSell.getUsableSize();
            assetToSell.setUsableSize(assetToSell.getUsableSize() - request.size());
            assetRepository.save(assetToSell);
            
            log.info("Asset balance updated for customer: {} - asset: {}, old: {}, new: {}, deducted: {}", 
                    request.customer(), request.asset(), oldUsableSize, assetToSell.getUsableSize(), request.size());

            Order order = saveOrder(request);
            log.info("SELL order created successfully with ID: {} for customer: {}", 
                    order.getId(), request.customer());
            
            return order;
            
        } catch (InvalidCustomerException e) {
            timer.finishWithError(e.getMessage());
            log.error("SELL order processing failed for customer: {} - Business Error: {}", 
                     request.customer(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("SELL order processing failed for customer: {} - System Error: {}", 
                     request.customer(), e.getMessage(), e);
            throw new BrokageFirmApiException("SELL order processing failed due to system error", e);
        } finally {
            timer.finish();
        }
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

