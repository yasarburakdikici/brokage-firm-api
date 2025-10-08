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

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class BuyCreateOrderProcessor implements CreateOrderProcessor {

    private static final Logger log = LoggerFactory.getLogger(BuyCreateOrderProcessor.class);
    
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
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("BUY_ORDER_PROCESS", log);
        
        try {
            log.info("Processing BUY order for customer: {}, asset: {}, size: {}, price: {}", 
                    request.customer(), request.asset(), request.size(), request.price());
            
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(request.customer(), "TRY")
                    .orElseThrow(() -> new InvalidCustomerException(
                            request.customer() + " does not have a TRY asset."
                    ));

            Long totalCost = request.price().multiply(BigDecimal.valueOf(request.size())).longValue();
            log.info("Total cost calculated: {} TRY for customer: {}", totalCost, request.customer());

            if (tryAsset.getUsableSize() < totalCost) {
                String errorMsg = String.format("Insufficient TRY balance for customer %s.",
                        request.customer());
                log.warn("Insufficient balance: {}", errorMsg);
                throw new InvalidCustomerException(errorMsg);
            }

            Long oldUsableSize = tryAsset.getUsableSize();
            tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost);
            assetRepository.save(tryAsset);
            
            log.info("TRY balance updated for customer: {} - old: {}, new: {}, deducted: {}", 
                    request.customer(), oldUsableSize, tryAsset.getUsableSize(), totalCost);

            Order order = saveOrder(request);
            log.info("BUY order created successfully with ID: {} for customer: {}", 
                    order.getId(), request.customer());
            
            return order;
            
        } catch (InvalidCustomerException e) {
            timer.finishWithError(e.getMessage());
            log.error("BUY order processing failed for customer: {} - Business Error: {}", 
                     request.customer(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("BUY order processing failed for customer: {} - System Error: {}", 
                     request.customer(), e.getMessage(), e);
            throw new BrokageFirmApiException("BUY order processing failed due to system error", e);
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

