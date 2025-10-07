package com.brokage.challenge.service.impl;

import com.brokage.challenge.entity.Asset;
import com.brokage.challenge.exception.InvalidAssetException;
import com.brokage.challenge.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {

    // Static test data
    private static final String TEST_CUSTOMER = "cust1";
    private static final String TEST_ASSET = "BTC";
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("5.00");
    private static final BigDecimal TEST_INITIAL = new BigDecimal("10.00");
    private static final String TEST_ASSET1 = "BTC";
    private static final String TEST_ASSET2 = "ETH";
    private static final BigDecimal TEST_SIZE = new BigDecimal("10.00");
    private static final BigDecimal TEST_USABLE = new BigDecimal("7.50");

    @Mock
    private AssetRepository assetRepository;

    private AssetServiceImpl assertService;

    @BeforeEach
    void setUp() {
        assertService = new AssetServiceImpl(assetRepository);
    }

    @Test
    @DisplayName("increaseUsableSize adds amount and saves asset")
    void increaseUsableSize_success() {
        // arrange
        Asset asset = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .usableSize(TEST_INITIAL)
                .build();

        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, TEST_ASSET))
                .thenReturn(Optional.of(asset));

        // act
        assertService.increaseUsableSize(TEST_CUSTOMER, TEST_ASSET, TEST_AMOUNT);

        // assert
        assertThat(asset.getUsableSize()).isEqualTo(TEST_INITIAL.add(TEST_AMOUNT));
        verify(assetRepository, times(1)).save(asset);
    }

    @Test
    @DisplayName("increaseUsableSize throws when asset not found")
    void increaseUsableSize_assetNotFound_throws() {
        // arrange
        when(assetRepository.findByCustomerIdAndAssetName(TEST_CUSTOMER, TEST_ASSET))
                .thenReturn(Optional.empty());

        // act + assert
        InvalidAssetException ex = assertThrows(InvalidAssetException.class,
                () -> assertService.increaseUsableSize(TEST_CUSTOMER, TEST_ASSET, TEST_AMOUNT));
        assertThat(ex.getMessage()).contains("Customer " + TEST_CUSTOMER + " does not have the asset: " + TEST_ASSET);
        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("listAssets returns all assets for customer")
    void listAssets_success() {
        // arrange
        Asset asset1 = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET1)
                .size(TEST_SIZE)
                .usableSize(TEST_USABLE)
                .build();
        Asset asset2 = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET2)
                .size(TEST_SIZE)
                .usableSize(TEST_USABLE)
                .build();
        when(assetRepository.findByCustomerId(TEST_CUSTOMER)).thenReturn(List.of(asset1, asset2));

        // act
        List<Asset> result = assertService.listAssets(TEST_CUSTOMER);

        // assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCustomerId()).isEqualTo(TEST_CUSTOMER);
        assertThat(result.get(1).getCustomerId()).isEqualTo(TEST_CUSTOMER);
    }
}


