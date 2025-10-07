package com.brokage.challenge.controller;

import com.brokage.challenge.dto.AssetResponse;
import com.brokage.challenge.entity.Asset;
import com.brokage.challenge.service.AssetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetControllerTest {

    private static final String TEST_CUSTOMER = "cust1";
    private static final String TEST_ASSET = "BTC";
    private static final BigDecimal TEST_SIZE = new BigDecimal("10.00");
    private static final BigDecimal TEST_USABLE = new BigDecimal("7.50");

    @Mock
    private AssetService assetService;

    @InjectMocks
    private AssetController assetController;

    @Test
    @DisplayName("listAssets maps domain to response via mapper and returns list")
    void listAssets_success() {
        // arrange
        Asset asset = Asset.builder()
                .customerId(TEST_CUSTOMER)
                .assetName(TEST_ASSET)
                .size(TEST_SIZE)
                .usableSize(TEST_USABLE)
                .build();
        when(assetService.listAssets(TEST_CUSTOMER)).thenReturn(List.of(asset));

        // act
        List<AssetResponse> result = assetController.listAssets(TEST_CUSTOMER);

        // assert
        assertThat(result).hasSize(1);
        AssetResponse resp = result.getFirst();
        assertThat(resp.customerId()).isEqualTo(TEST_CUSTOMER);
        assertThat(resp.assetName()).isEqualTo(TEST_ASSET);
        assertThat(resp.totalSize()).isEqualTo(TEST_SIZE);
        assertThat(resp.usableSize()).isEqualTo(TEST_USABLE);
    }
}


