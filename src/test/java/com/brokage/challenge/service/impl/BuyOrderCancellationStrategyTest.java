package com.brokage.challenge.service.impl;

import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.service.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyOrderCancellationStrategyTest {

    // Static test data
    private static final String TEST_CUSTOMER = "cust1";
    private static final String TEST_ASSET = "BTC";
    private static final Long TEST_SIZE = 2L;
    private static final BigDecimal TEST_PRICE = new BigDecimal("10.50");
    private static final Long TEST_RESERVED_AMOUNT = 21L;

    @Mock
    private AssetService assetService;

    private BuyOrderCancellationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BuyOrderCancellationStrategy(assetService);
    }

    @Test
    @DisplayName("getSupportedSide returns BUY")
    void getSupportedSide_returnsBuy() {
        // act
        OrderSide result = strategy.getSupportedSide();

        // assert
        assertThat(result).isEqualTo(OrderSide.BUY);
    }

    @Test
    @DisplayName("refundUsableBalance increases TRY asset for buy order cancellation")
    void refundUsableBalance_success() {
        // arrange
        Order order = Order.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .orderSide(OrderSide.BUY)
                .size(TEST_SIZE)
                .price(TEST_PRICE)
                .build();

        // act
        strategy.refundUsableBalance(order);

        // assert
        verify(assetService, times(1)).increaseUsableSize(TEST_CUSTOMER, "TRY", TEST_RESERVED_AMOUNT);
    }
}
