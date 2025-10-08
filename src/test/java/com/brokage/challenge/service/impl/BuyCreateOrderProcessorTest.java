package com.brokage.challenge.service.impl;

import com.brokage.challenge.dto.CreateOrder;
import com.brokage.challenge.entity.Asset;
import com.brokage.challenge.entity.Order;
import com.brokage.challenge.enums.OrderSide;
import com.brokage.challenge.enums.OrderStatus;
import com.brokage.challenge.exception.BrokageFirmApiException;
import com.brokage.challenge.exception.InvalidCustomerException;
import com.brokage.challenge.repository.AssetRepository;
import com.brokage.challenge.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyCreateOrderProcessorTest {

    // Static test data
    private static final String TEST_CUSTOMER = "cust1";
    private static final String TEST_ASSET = "BTC";
    private static final Long TEST_SIZE = 2L;
    private static final BigDecimal TEST_PRICE = new BigDecimal("10.50");
    private static final BigDecimal TEST_TOTAL_COST = new BigDecimal("21.00");
    private static final Long TEST_TRY_BALANCE = 100L;
    private static final Long TEST_INSUFFICIENT_BALANCE = 10L;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private OrderRepository orderRepository;

    private BuyCreateOrderProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new BuyCreateOrderProcessor(assetRepository, orderRepository);
    }

    @Test
    @DisplayName("getSupportedSide returns BUY")
    void getSupportedSide_returnsBuy() {
        // act
        OrderSide result = processor.getSupportedSide();

        // assert
        assertThat(result).isEqualTo(OrderSide.BUY);
    }

    @Test
    @DisplayName("process creates order successfully with sufficient TRY balance")
    void process_success_sufficientBalance() {
        // arrange
        CreateOrder request = new CreateOrder(TEST_CUSTOMER, OrderSide.BUY, TEST_ASSET, TEST_SIZE, TEST_PRICE);
        Asset tryAsset = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName("TRY")
                .usableSize(TEST_TRY_BALANCE)
                .build();
        Order expectedOrder = Order.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .orderSide(OrderSide.BUY)
                .size(TEST_SIZE)
                .price(TEST_PRICE)
                .status(OrderStatus.PENDING)
                .createDate(Instant.now())
                .build();

        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, "TRY")).thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // act
        Order result = processor.process(request);

        // assert
        assertThat(result).isEqualTo(expectedOrder);
        assertThat(tryAsset.getUsableSize()).isEqualTo(TEST_TRY_BALANCE - TEST_TOTAL_COST.longValue());
        verify(assetRepository, times(1)).save(tryAsset);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("process throws when TRY asset not found")
    void process_throws_whenTryAssetNotFound() {
        // arrange
        CreateOrder request = new CreateOrder(TEST_CUSTOMER, OrderSide.BUY, TEST_ASSET, TEST_SIZE, TEST_PRICE);
        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, "TRY")).thenReturn(Optional.empty());

        // act + assert
        InvalidCustomerException ex = assertThrows(InvalidCustomerException.class, () -> processor.process(request));
        assertThat(ex.getMessage()).contains(TEST_CUSTOMER + " does not have a TRY asset.");
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("process throws when insufficient TRY balance")
    void process_throws_insufficientBalance() {
        // arrange
        CreateOrder request = new CreateOrder(TEST_CUSTOMER, OrderSide.BUY, TEST_ASSET, TEST_SIZE, TEST_PRICE);
        Asset tryAsset = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName("TRY")
                .usableSize(TEST_INSUFFICIENT_BALANCE)
                .build();

        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, "TRY")).thenReturn(Optional.of(tryAsset));

        // act + assert
        InvalidCustomerException ex = assertThrows(InvalidCustomerException.class, () -> processor.process(request));
        assertThat(ex.getMessage()).contains("Insufficient TRY balance for customer " + TEST_CUSTOMER);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("process should throw BrokageFirmApiException when repository throws unexpected exception")
    void process_WhenRepositoryThrowsUnexpectedException_ShouldThrowBrokageFirmApiException() {
        // arrange
        CreateOrder request = new CreateOrder(TEST_CUSTOMER, OrderSide.BUY, TEST_ASSET, TEST_SIZE, TEST_PRICE);
        Asset tryAsset = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName("TRY")
                .size(TEST_TRY_BALANCE)
                .usableSize(TEST_TRY_BALANCE)
                .build();
        
        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, "TRY")).thenReturn(Optional.of(tryAsset));
        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        when(assetRepository.save(any(Asset.class))).thenThrow(repositoryException);

        // act & assert
        BrokageFirmApiException exception = assertThrows(BrokageFirmApiException.class, 
            () -> processor.process(request));
        
        assertThat(exception.getMessage()).contains("BUY order processing failed due to system error");
        assertThat(exception.getCause()).isEqualTo(repositoryException);
    }

    @Test
    @DisplayName("process should throw BrokageFirmApiException when orderRepository throws exception")
    void process_WhenOrderRepositoryThrowsException_ShouldThrowBrokageFirmApiException() {
        // arrange
        CreateOrder request = new CreateOrder(TEST_CUSTOMER, OrderSide.BUY, TEST_ASSET, TEST_SIZE, TEST_PRICE);
        Asset tryAsset = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName("TRY")
                .size(TEST_TRY_BALANCE)
                .usableSize(TEST_TRY_BALANCE)
                .build();
        
        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, "TRY")).thenReturn(Optional.of(tryAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(tryAsset);
        RuntimeException orderRepositoryException = new RuntimeException("Order repository error");
        when(orderRepository.save(any(Order.class))).thenThrow(orderRepositoryException);

        // act & assert
        BrokageFirmApiException exception = assertThrows(BrokageFirmApiException.class, 
            () -> processor.process(request));
        
        assertThat(exception.getMessage()).contains("BUY order processing failed due to system error");
        assertThat(exception.getCause()).isEqualTo(orderRepositoryException);
    }
}
