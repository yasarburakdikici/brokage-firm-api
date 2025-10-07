package com.brokage.challenge.controller;

import com.brokage.challenge.dto.AssetResponse;
import com.brokage.challenge.mapper.AssetResponseMapper;
import com.brokage.challenge.service.AssetService;
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

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public List<AssetResponse> listAssets(@RequestParam String customerId) {
        return assetService.listAssets(customerId).stream().map(AssetResponseMapper::toAssetResponse).toList();
    }

}
