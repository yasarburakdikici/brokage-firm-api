package com.brokage.challenge.dto;

import java.math.BigDecimal;

import org.springframework.validation.annotation.Validated;

import com.brokage.challenge.enums.OrderSide;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
public record CreateOrder(
            @NotNull String customer,
            @NotNull OrderSide side,
            @NotNull String asset,
            @NotNull @Min(value = 1, message = "Size must be at least 1") Long size, 
            @NotNull @DecimalMin(value = "0.01", message = "Price must be at least 0.01") BigDecimal price
    ) {}