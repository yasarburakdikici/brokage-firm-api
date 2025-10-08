package com.brokage.challenge.service;

import com.brokage.challenge.entity.Asset;

import java.util.List;

public interface AssetService {
    void increaseUsableSize(String customerId, String assetName, Long amount);
    List<Asset> listAssets(String customerId);
}
