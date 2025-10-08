package com.brokage.challenge.service.impl;

import com.brokage.challenge.entity.Asset;
import com.brokage.challenge.exception.InvalidAssetException;
import com.brokage.challenge.repository.AssetRepository;
import org.springframework.stereotype.Service;

import com.brokage.challenge.service.AssetService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssetServiceImpl implements AssetService {
    private final AssetRepository assetRepository;

    public AssetServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Transactional
    @Override
    public void increaseUsableSize(String customerId, String assetName, Long amount) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new InvalidAssetException(String.format("Customer %s does not have the asset: %s", customerId, assetName)));
        asset.setUsableSize(asset.getUsableSize() + amount);
        assetRepository.save(asset);
    }

    @Override
    public List<Asset> listAssets(String customerId) {
        return assetRepository.findByCustomerId(customerId);
    }
}


