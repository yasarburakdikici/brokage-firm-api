package com.brokage.challenge.dto;

import java.math.BigDecimal;

public record AssetResponse (String customerId, String assetName, BigDecimal totalSize, BigDecimal usableSize) {
}
