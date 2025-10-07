package com.brokage.challenge.service.impl;

import com.brokage.challenge.dto.CreateOrder;
import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.enums.OrderStatus;
import com.brokage.challenge.exception.InvalidOrderException;
import com.brokage.challenge.repository.OrderRepository;
import com.brokage.challenge.service.CreateOrderProcessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    // Static test data
    private static final String TEST_CUSTOMER = "cust1";
    private static final String TEST_ASSET = "BTC";
    private static final Long TEST_SIZE = 2L;
    private static final BigDecimal TEST_PRICE = new BigDecimal("10.50");
    private static final Long TEST_ORDER_ID = 1L;
    private static final Long TEST_ORDER_ID_NOT_FOUND = 99L;
    private static final Long TEST_ORDER_ID_NOT_PENDING = 11L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetUpdateManager assetUpdateManager;

    @Mock
    private CreateOrderProcessor buyProcessor;

    @Mock
    private CreateOrderProcessor sellProcessor;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        when(buyProcessor.getSupportedSide()).thenReturn(OrderSide.BUY);
        when(sellProcessor.getSupportedSide()).thenReturn(OrderSide.SELL);
        orderService = new OrderServiceImpl(List.of(buyProcessor, sellProcessor), orderRepository, assetUpdateManager);
    }

    private CreateOrder prepareCreateOrder(String customer, OrderSide side, String asset, Long size, BigDecimal price) {
        return new CreateOrder(customer, side, asset, size, price);
    }

    private CreateOrder prepareCreateOrder(OrderSide side) {
        return new CreateOrder(TEST_CUSTOMER, side, TEST_ASSET, TEST_SIZE, TEST_PRICE);
    }

    @Test
    @DisplayName("createOrder delegates to BUY processor and returns order")
    void createOrder_buy_success() {
        // arrange
        CreateOrder request = prepareCreateOrder(OrderSide.BUY);
        Order expected = Order.builder()
                .id(TEST_ORDER_ID)
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .orderSide(OrderSide.BUY)
                .size(TEST_SIZE)
                .price(TEST_PRICE)
                .status(OrderStatus.PENDING)
                .createDate(Instant.now())
                .build();

        when(buyProcessor.process(request)).thenReturn(expected);
        
        // act
        Order result = orderService.createOrder(request);

        // assert
        assertThat(result).isEqualTo(expected);
        verify(buyProcessor, times(1)).process(request);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("createOrder throws on unsupported side")
    void createOrder_unsupportedSide_throws() {
        // arrange
        CreateOrder request = prepareCreateOrder(TEST_CUSTOMER, null, TEST_ASSET, TEST_SIZE, TEST_PRICE);

        // act + assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
        assertThat(ex.getMessage()).contains("Unsupported order side");
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("deleteOrder deletes PENDING order and refunds usable balance")
    void deleteOrder_pending_success() {
        // arrange
        Order pending = Order.builder()
                .id(TEST_ORDER_ID)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(pending));
        
        // act
        orderService.deleteOrder(TEST_ORDER_ID);

        // assert
        verify(assetUpdateManager, times(1)).refundUsableBalanceForCancellation(pending);
        verify(orderRepository, times(1)).delete(pending);
    }

    @Test
    @DisplayName("deleteOrder throws when order not found")
    void deleteOrder_notFound_throws() {
        // arrange
        when(orderRepository.findById(TEST_ORDER_ID_NOT_FOUND)).thenReturn(Optional.empty());

        // act + assert
        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.deleteOrder(TEST_ORDER_ID_NOT_FOUND));
        assertThat(ex.getMessage()).contains("Order not found for this order id: " + TEST_ORDER_ID_NOT_FOUND);
        verify(orderRepository, never()).delete(any());
        verifyNoInteractions(assetUpdateManager);
    }

    @Test
    @DisplayName("deleteOrder throws when order is not PENDING")
    void deleteOrder_notPending_throws() {
        // arrange
        Order executed = Order.builder()
                .id(TEST_ORDER_ID_NOT_PENDING)
                .status(OrderStatus.EXECUTED)
                .build();

        when(orderRepository.findById(TEST_ORDER_ID_NOT_PENDING)).thenReturn(Optional.of(executed));

        // act + assert
        InvalidOrderException ex = assertThrows(InvalidOrderException.class, () -> orderService.deleteOrder(TEST_ORDER_ID_NOT_PENDING));
        assertThat(ex.getMessage()).contains("Order not found for this order id: " + TEST_ORDER_ID_NOT_PENDING);
        verify(orderRepository, never()).delete(any());
        verifyNoInteractions(assetUpdateManager);
    }

    @Test
    @DisplayName("listOrders returns orders for given customer and date range")
    void listOrder_success() {
        // arrange
        Instant startDate = Instant.parse("2023-01-01T00:00:00Z");
        Instant endDate = Instant.parse("2023-12-31T23:59:59Z");

        List<Order> expectedOrders = List.of(
                Order.builder().id(1L).customerId(TEST_CUSTOMER).createDate(Instant.parse("2023-06-15T10:00:00Z")).build(),
                Order.builder().id(2L).customerId(TEST_CUSTOMER).createDate(Instant.parse("2023-07-20T15:30:00Z")).build()
        );

        when(orderRepository.findByCustomerIdAndCreateDateBetween(TEST_CUSTOMER, startDate, endDate))
                .thenReturn(expectedOrders);

        // act
        List<Order> result = orderService.listOrders(TEST_CUSTOMER, startDate, endDate);

        // assert
        assertThat(result).isEqualTo(expectedOrders);
        verify(orderRepository, times(1)).findByCustomerIdAndCreateDateBetween(TEST_CUSTOMER, startDate, endDate);
    }
}


