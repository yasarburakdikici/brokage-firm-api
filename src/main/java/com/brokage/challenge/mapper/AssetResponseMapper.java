package com.brokage.challenge.mapper;

import com.brokage.challenge.dto.AssetResponse;
import com.brokage.challenge.entity.Asset;

public class AssetResponseMapper {
    private AssetResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static AssetResponse toAssetResponse(Asset asset) {
        return new AssetResponse(
                asset.getCustomerId(),
                asset.getAssetName(),
                asset.getSize(),
                asset.getUsableSize()
        );
    }
}
