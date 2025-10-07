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
class SellOrderCancellationStrategyTest {

    // Static test data
    private static final String TEST_CUSTOMER = "cust1";
    private static final String TEST_ASSET = "BTC";
    private static final Long TEST_SIZE = 2L;
    private static final BigDecimal TEST_PRICE = new BigDecimal("10.50");
    private static final BigDecimal TEST_RESERVED_SHARES = BigDecimal.valueOf(TEST_SIZE);

    @Mock
    private AssetService assetService;

    private SellOrderCancellationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SellOrderCancellationStrategy(assetService);
    }

    @Test
    @DisplayName("getSupportedSide returns SELL")
    void getSupportedSide_returnsSell() {
        // act
        OrderSide result = strategy.getSupportedSide();

        // assert
        assertThat(result).isEqualTo(OrderSide.SELL);
    }

    @Test
    @DisplayName("refundUsableBalance increases asset for sell order cancellation")
    void refundUsableBalance_success() {
        // arrange
        Order order = Order.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .orderSide(OrderSide.SELL)
                .size(TEST_SIZE)
                .price(TEST_PRICE)
                .build();

        // act
        strategy.refundUsableBalance(order);

        // assert
        verify(assetService, times(1)).increaseUsableSize(TEST_CUSTOMER, TEST_ASSET, TEST_RESERVED_SHARES);
    }
}
