package com.brokage.challenge.controller;

import com.brokage.challenge.dto.AssetResponse;
import com.brokage.challenge.mapper.AssetResponseMapper;
import com.brokage.challenge.exception.BrokageFirmApiException;
import com.brokage.challenge.service.AssetService;
import com.brokage.challenge.util.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/asset")
public class AssetController {

    private static final Logger log = LoggerFactory.getLogger(AssetController.class);
    
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public List<AssetResponse> listAssets(@RequestParam String customerId) {
        TimeUtil.ExecutionTimer timer = TimeUtil.startTimer("LIST_ASSETS_CONTROLLER", log);
        
        try {
            log.info("Asset list request received for customer: {}", customerId);
            
            List<AssetResponse> responses = assetService.listAssets(customerId)
                    .stream()
                    .map(AssetResponseMapper::toAssetResponse)
                    .toList();
            
            log.info("Asset list response prepared for customer: {}, found {} assets", 
                    customerId, responses.size());
            
            return responses;
            
        } catch (Exception e) {
            timer.finishWithError(e.getMessage());
            log.error("Asset list request failed for customer: {} - System Error: {}", 
                     customerId, e.getMessage(), e);
            throw new BrokageFirmApiException("Asset list request failed due to system error", e);
        } finally {
            timer.finish();
        }
    }

}
