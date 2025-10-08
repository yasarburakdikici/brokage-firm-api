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
class SellCreateOrderProcessorTest {

    // Static test data
    private static final String TEST_CUSTOMER = "cust1";
    private static final String TEST_ASSET = "BTC";
    private static final Long TEST_SIZE = 2L;
    private static final BigDecimal TEST_PRICE = new BigDecimal("10.50");
    private static final Long TEST_ASSET_BALANCE = 5L;
    private static final Long TEST_INSUFFICIENT_BALANCE = 1L;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private OrderRepository orderRepository;

    private SellCreateOrderProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new SellCreateOrderProcessor(assetRepository, orderRepository);
    }

    @Test
    @DisplayName("getSupportedSide returns SELL")
    void getSupportedSide_returnsSell() {
        // act
        OrderSide result = processor.getSupportedSide();

        // assert
        assertThat(result).isEqualTo(OrderSide.SELL);
    }

    @Test
    @DisplayName("process creates order successfully with sufficient asset balance")
    void process_success_sufficientBalance() {
        // arrange
        CreateOrder request = new CreateOrder(TEST_CUSTOMER, OrderSide.SELL, TEST_ASSET, TEST_SIZE, TEST_PRICE);
        Asset assetToSell = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .usableSize(TEST_ASSET_BALANCE)
                .build();
        Order expectedOrder = Order.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .orderSide(OrderSide.SELL)
                .size(TEST_SIZE)
                .price(TEST_PRICE)
                .status(OrderStatus.PENDING)
                .createDate(Instant.now())
                .build();

        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, TEST_ASSET)).thenReturn(Optional.of(assetToSell));
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // act
        Order result = processor.process(request);

        // assert
        assertThat(result).isEqualTo(expectedOrder);
        assertThat(assetToSell.getUsableSize()).isEqualTo(TEST_ASSET_BALANCE - TEST_SIZE);
        verify(assetRepository, times(1)).save(assetToSell);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("process throws when asset not found")
    void process_throws_whenAssetNotFound() {
        // arrange
        CreateOrder request = new CreateOrder(TEST_CUSTOMER, OrderSide.SELL, TEST_ASSET, TEST_SIZE, TEST_PRICE);
        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, TEST_ASSET)).thenReturn(Optional.empty());

        // act + assert
        InvalidCustomerException ex = assertThrows(InvalidCustomerException.class, () -> processor.process(request));
        assertThat(ex.getMessage()).contains(TEST_CUSTOMER + " does not have a " + TEST_ASSET + " asset.");
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("process throws when insufficient asset balance")
    void process_throws_insufficientBalance() {
        // arrange
        CreateOrder request = new CreateOrder(TEST_CUSTOMER, OrderSide.SELL, TEST_ASSET, TEST_SIZE, TEST_PRICE);
        Asset assetToSell = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .usableSize(TEST_INSUFFICIENT_BALANCE)
                .build();

        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, TEST_ASSET)).thenReturn(Optional.of(assetToSell));

        // act + assert
        InvalidCustomerException ex = assertThrows(InvalidCustomerException.class, () -> processor.process(request));
        assertThat(ex.getMessage()).contains("Insufficient " + TEST_ASSET + " balance for customer " + TEST_CUSTOMER);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("process should throw BrokageFirmApiException when repository throws unexpected exception")
    void process_WhenRepositoryThrowsUnexpectedException_ShouldThrowBrokageFirmApiException() {
        // arrange
        CreateOrder request = new CreateOrder(TEST_CUSTOMER, OrderSide.SELL, TEST_ASSET, TEST_SIZE, TEST_PRICE);
        Asset assetToSell = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .size(TEST_ASSET_BALANCE)
                .usableSize(TEST_ASSET_BALANCE)
                .build();
        
        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, TEST_ASSET)).thenReturn(Optional.of(assetToSell));
        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        when(assetRepository.save(any(Asset.class))).thenThrow(repositoryException);

        // act & assert
        BrokageFirmApiException exception = assertThrows(BrokageFirmApiException.class, 
            () -> processor.process(request));
        
        assertThat(exception.getMessage()).contains("SELL order processing failed due to system error");
        assertThat(exception.getCause()).isEqualTo(repositoryException);
    }

    @Test
    @DisplayName("process should throw BrokageFirmApiException when orderRepository throws exception")
    void process_WhenOrderRepositoryThrowsException_ShouldThrowBrokageFirmApiException() {
        // arrange
        CreateOrder request = new CreateOrder(TEST_CUSTOMER, OrderSide.SELL, TEST_ASSET, TEST_SIZE, TEST_PRICE);
        Asset assetToSell = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .size(TEST_ASSET_BALANCE)
                .usableSize(TEST_ASSET_BALANCE)
                .build();
        
        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, TEST_ASSET)).thenReturn(Optional.of(assetToSell));
        when(assetRepository.save(any(Asset.class))).thenReturn(assetToSell);
        RuntimeException orderRepositoryException = new RuntimeException("Order repository error");
        when(orderRepository.save(any(Order.class))).thenThrow(orderRepositoryException);

        // act & assert
        BrokageFirmApiException exception = assertThrows(BrokageFirmApiException.class, 
            () -> processor.process(request));
        
        assertThat(exception.getMessage()).contains("SELL order processing failed due to system error");
        assertThat(exception.getCause()).isEqualTo(orderRepositoryException);
    }
}
