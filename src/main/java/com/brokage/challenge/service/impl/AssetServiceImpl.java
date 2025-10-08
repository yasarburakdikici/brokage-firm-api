package com.brokage.challenge.service.impl;

import com.brokage.challenge.entity.Asset;
import com.brokage.challenge.exception.BrokageFirmApiException;
import com.brokage.challenge.exception.InvalidAssetException;
import com.brokage.challenge.repository.AssetRepository;
import com.brokage.challenge.service.AssetService;
import com.brokage.challenge.util.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssetServiceImpl implements AssetService {
    
    private static final Logger log = LoggerFactory.getLogger(AssetServiceImpl.class);
    
    private final AssetRepository assetRepository;

    public AssetServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Transactional
    @Override
    public void increaseUsableSize(String customerId, String assetName, Long amount) {
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("INCREASE_USABLE_SIZE", log);
        
        try {
            log.info("Increasing usable size for customer: {}, asset: {}, amount: {}", 
                    customerId, assetName, amount);
            
            Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                    .orElseThrow(() -> new InvalidAssetException(String.format("Customer %s does not have the asset: %s", customerId, assetName)));
            
            Long oldUsableSize = asset.getUsableSize();
            asset.setUsableSize(asset.getUsableSize() + amount);
            assetRepository.save(asset);
            
            log.info("Asset updated - customer: {}, asset: {}, old usable size: {}, new usable size: {}", 
                    customerId, assetName, oldUsableSize, asset.getUsableSize());
            
        } catch (InvalidAssetException e) {
            timer.finishWithError(e.getMessage());
            log.error("Failed to increase usable size for customer: {}, asset: {} - Business Error: {}", 
                     customerId, assetName, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("Failed to increase usable size for customer: {}, asset: {} - System Error: {}", 
                     customerId, assetName, e.getMessage(), e);
            throw new BrokageFirmApiException("Failed to increase usable size due to system error", e);
        } finally {
            timer.finish();
        }
    }

    @Override
    public List<Asset> listAssets(String customerId) {
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("LIST_ASSETS", log);
        
        try {
            log.info("Listing assets for customer: {}", customerId);
            
            List<Asset> assets = assetRepository.findByCustomerId(customerId);
            
            log.info("Found {} assets for customer: {}", assets.size(), customerId);
            return assets;
            
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("Failed to list assets for customer: {} - System Error: {}", 
                     customerId, e.getMessage(), e);
            throw new BrokageFirmApiException("Failed to list assets due to system error", e);
        } finally {
            timer.finish();
        }
    }
}


