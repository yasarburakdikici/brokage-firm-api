package com.brokage.challenge.service.impl;

import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.exception.InvalidOrderException;
import com.brokage.challenge.service.OrderCancellationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetUpdateManagerTest {

    // Static test data
    private static final String TEST_CUSTOMER = "cust1";
    private static final String TEST_ASSET = "BTC";
    private static final Long TEST_SIZE = 2L;

    @Mock
    private OrderCancellationStrategy buyStrategy;

    @Mock
    private OrderCancellationStrategy sellStrategy;

    private AssetUpdateManager assetUpdateManager;

    @BeforeEach
    void setUp() {
        when(buyStrategy.getSupportedSide()).thenReturn(OrderSide.BUY);
        when(sellStrategy.getSupportedSide()).thenReturn(OrderSide.SELL);
        assetUpdateManager = new AssetUpdateManager(List.of(buyStrategy, sellStrategy));
    }

    private Order buildOrder(OrderSide side) {
        return Order.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .orderSide(side)
                .size(TEST_SIZE)
                .createDate(Instant.now())
                .build();
    }

    @Test
    @DisplayName("refundUsableBalanceForCancellation delegates to BUY strategy")
    void refundUsableBalanceForCancellation_buy_delegates() {
        // arrange
        Order order = buildOrder(OrderSide.BUY);

        // act
        assetUpdateManager.refundUsableBalanceForCancellation(order);

        // assert
        verify(buyStrategy, times(1)).refundUsableBalance(order);
    }

    @Test
    @DisplayName("refundUsableBalanceForCancellation delegates to SELL strategy")
    void refundUsableBalanceForCancellation_sell_delegates() {
        // arrange
        Order order = buildOrder(OrderSide.SELL);

        // act
        assetUpdateManager.refundUsableBalanceForCancellation(order);

        // assert
        verify(sellStrategy, times(1)).refundUsableBalance(order);
    }

    @Test
    @DisplayName("refundUsableBalanceForCancellation throws when strategy missing")
    void refundUsableBalanceForCancellation_missingStrategy_throws() {
        // arrange: manager with only BUY strategy
        assetUpdateManager = new AssetUpdateManager(List.of(buyStrategy));
        Order order = buildOrder(OrderSide.SELL);

        // act + assert
        InvalidOrderException ex = assertThrows(InvalidOrderException.class,
                () -> assetUpdateManager.refundUsableBalanceForCancellation(order));
        assertThat(ex.getMessage()).contains("No cancellation strategy found for order side: SELL");
    }
}


