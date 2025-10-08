package com.brokage.challenge.dto;

public record AssetResponse (String customerId, String assetName, Long totalSize, Long usableSize) {
}
